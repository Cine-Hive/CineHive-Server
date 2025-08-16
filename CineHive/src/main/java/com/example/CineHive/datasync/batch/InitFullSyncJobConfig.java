package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.batch.tasklet.ExportDownloadTasklet;
import com.example.CineHive.datasync.batch.writer.MovieSyncWriter;
import com.example.CineHive.datasync.batch.writer.TvSyncWriter;
import com.example.CineHive.datasync.batch.writer.PersonSyncWriter;
import com.example.CineHive.datasync.batch.writer.TmdbExportWriter;
import com.example.CineHive.datasync.domain.entity.*;
import com.example.CineHive.datasync.domain.service.MovieSyncService;
import com.example.CineHive.datasync.domain.service.TvSyncService;
import com.example.CineHive.datasync.domain.service.PersonSyncService;
import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.dto.TvDelta;
import com.example.CineHive.datasync.dto.PersonDelta;
import com.example.CineHive.datasync.dto.TmdbExportItem;
import com.example.CineHive.datasync.dto.WorkQueueRow;
import com.example.CineHive.global.exception.TmdbClientException;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitFullSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private final TmdbApiClient tmdbApiClient;
    private final MovieSyncService movieSyncService;
    private final TvSyncService tvSyncService;
    private final PersonSyncService personSyncService;
    private final ExportDownloadTasklet exportDownloadTasklet;
    private final TmdbExportWriter tmdbExportWriter;
    private final MovieSyncWriter movieSyncWriter;
    private final TvSyncWriter tvSyncWriter;
    private final PersonSyncWriter personSyncWriter;
    private final ObjectMapper objectMapper;

    private static final String EXPORT_URL_TEMPLATE = "http://files.tmdb.org/p/exports/%s_ids_%s.json.gz";

    @Bean("fullSyncJob")
    public Job fullSyncJob() {
        return new JobBuilder("fullSyncJob", jobRepository)
                .start(exportDownloadStep())
                .next(exportSeedingStep())
                .next(queueCountProbeStep())  // Debug step to count queue
                .next(movieDetailStep())
                .next(tvDetailStep())
                .next(personDetailStep())
                .build();
    }

    @Bean
    public Step exportDownloadStep() {
        return new StepBuilder("exportDownloadStep", jobRepository)
                .tasklet(exportDownloadTasklet, transactionManager)
                .build();
    }
    
    @Bean
    public Step exportSeedingStep() {
        return new StepBuilder("exportSeedingStep", jobRepository)
                .<TmdbExportItem, TmdbExportItem>chunk(5000, transactionManager)
                .reader(exportReader(null))
                .writer(tmdbExportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    public Step movieDetailStep() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2);
        
        return new StepBuilder("movieDetailStep", jobRepository)
                .<WorkQueueRow, MovieDelta>chunk(20, transactionManager)  // FK 위반 디버깅을 위해 chunk 크기 축소
                .reader(movieWorkQueueReader())
                .processor(movieDetailProcessor())
                .writer(movieSyncWriter)
                .listener(new ItemReadListener<WorkQueueRow>() {
                    @Override
                    public void afterRead(WorkQueueRow item) {
                        log.info("=== AFTER READ === tmdbId={}, entityType={}", item.tmdbId(), item.entityType());
                    }
                    @Override
                    public void onReadError(Exception ex) {
                        log.error("=== READ ERROR ===", ex);
                    }
                })
                .listener(new ItemProcessListener<WorkQueueRow, MovieDelta>() {
                    @Override
                    public void beforeProcess(WorkQueueRow item) {
                        log.info("=== BEFORE PROCESS === tmdbId={}", item.tmdbId());
                    }
                    @Override
                    public void afterProcess(WorkQueueRow item, MovieDelta result) {
                        log.info("=== AFTER PROCESS === tmdbId={}, result={}", item.tmdbId(), result != null ? "SUCCESS" : "NULL");
                    }
                    @Override
                    public void onProcessError(WorkQueueRow item, Exception e) {
                        log.error("=== PROCESS ERROR === tmdbId={}", item.tmdbId(), e);
                    }
                })
                .listener(new ItemWriteListener<MovieDelta>() {
                    @Override
                    public void beforeWrite(org.springframework.batch.item.Chunk<? extends MovieDelta> items) {
                        log.info("=== BEFORE WRITE === count={}", items.size());
                    }
                    @Override
                    public void afterWrite(org.springframework.batch.item.Chunk<? extends MovieDelta> items) {
                        log.info("=== AFTER WRITE === count={}", items.size());
                    }
                    @Override
                    public void onWriteError(Exception exception, org.springframework.batch.item.Chunk<? extends MovieDelta> items) {
                        log.error("=== WRITE ERROR === count={}", items.size(), exception);
                    }
                })
                .taskExecutor(batchTaskExecutor())
                .faultTolerant()
                .retry(IOException.class)
                .retry(TmdbClientException.class)
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TmdbExportItem> exportReader(
            @Value("#{jobExecutionContext['exportPath']}") String exportPath) {
        
        // Validate that exportPath was provided
        if (exportPath == null || exportPath.isBlank()) {
            throw new IllegalStateException("exportPath is missing from JobExecutionContext. " +
                    "ExportDownloadTasklet should have set this value.");
        }
        
        log.info("Creating export reader with path from JobExecutionContext: {}", exportPath);
        
        FlatFileItemReader<TmdbExportItem> reader = new FlatFileItemReader<>();
        reader.setName("exportReader");
        reader.setResource(new FileSystemResource(exportPath));
        
        // NDJSON: each line is a separate JSON object
        reader.setLineMapper(new LineMapper<TmdbExportItem>() {
            @Override
            public TmdbExportItem mapLine(String line, int lineNumber) throws Exception {
                return objectMapper.readValue(line, TmdbExportItem.class);
            }
        });
        
        // Keep strict mode true to fail fast if file doesn't exist
        reader.setStrict(true);
        return reader;
    }

    @Bean
    public Step queueCountProbeStep() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new StepBuilder("queueCountProbeStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                Long totalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_work_queue", 
                    Long.class
                );
                Long movieCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_work_queue WHERE UPPER(entity_type) = 'MOVIE'", 
                    Long.class
                );
                Long unprocessedMovieCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_work_queue WHERE UPPER(entity_type) = 'MOVIE' AND processed = false", 
                    Long.class
                );
                
                log.info("=== QUEUE PROBE RESULTS ===");
                log.info("Total records: {}", totalCount);
                log.info("Movie records: {}", movieCount);
                log.info("Unprocessed movie records: {}", unprocessedMovieCount);
                log.info("===========================");
                
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<WorkQueueRow> movieWorkQueueReader() {
        log.info("Creating JdbcCursorItemReader for movieWorkQueueReader");
        
        String sql = """
            SELECT entity_type, tmdb_id, priority, processed
            FROM tmdb_work_queue
            WHERE UPPER(entity_type) = 'MOVIE'
              AND processed = false
            ORDER BY priority DESC, tmdb_id ASC
            LIMIT 10000
        """;
        
        log.info("SQL Query: {}", sql);
        
        return new JdbcCursorItemReaderBuilder<WorkQueueRow>()
                .name("movieWorkQueueReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    WorkQueueRow row = new WorkQueueRow(
                        rs.getString("entity_type"),
                        rs.getLong("tmdb_id"),
                        rs.getInt("priority"),
                        rs.getBoolean("processed")
                    );
                    log.debug("Read row: {}", row);
                    return row;
                })
                .fetchSize(100)
                .verifyCursorPosition(false)
                .build();
    }

    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);  // 동시성 감소로 FK 위반 디버깅 용이
        executor.setMaxPoolSize(4);   // 동시성 감소로 FK 위반 디버깅 용이
        executor.setQueueCapacity(50); // 큐 크기도 감소
        executor.setThreadNamePrefix("batch-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public ItemProcessor<WorkQueueRow, MovieDelta> movieDetailProcessor() {
        return workItem -> {
            log.info("=== PROCESSOR CALLED ===");
            log.info("Processing WorkQueueRow: entityType={}, tmdbId={}, priority={}, processed={}", 
                workItem.entityType(), workItem.tmdbId(), workItem.priority(), workItem.processed());
            
            // MOVIE 타입만 처리 (이미 리더에서 필터링 되지만 안전을 위해)
            if (!"MOVIE".equalsIgnoreCase(workItem.entityType())) {
                log.debug("Skipping non-movie entity: type={}, id={}", workItem.entityType(), workItem.tmdbId());
                return null;
            }
            
            Long movieId = workItem.tmdbId();
            log.info("Processing Movie ID: {}", movieId);

            TmdbMovieDetailResponse response = tmdbApiClient.getMovieDetail(movieId);
            if (response == null) {
                log.warn("Movie ID {} not found or failed to fetch.", movieId);
                return null;
            }
            
            // Log credits info
            if (response.credits() != null) {
                log.info("Movie {} has credits: cast={}, crew={}", 
                    movieId, 
                    response.credits().cast() != null ? response.credits().cast().size() : 0,
                    response.credits().crew() != null ? response.credits().crew().size() : 0);
            } else {
                log.warn("Movie {} has NO credits in response", movieId);
            }

            // MovieDelta의 static factory 메서드를 사용하여 변환
            MovieDelta delta = MovieDelta.fromTmdbResponse(response);
            log.info("MovieDelta created for movie {}: persons={}", movieId, 
                delta.persons() != null ? delta.persons().size() : 0);
            return delta;
        };
    }


    // --- Helper Methods ---
    
    private LocalDate parseDate(String dateStr, Long movieId) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date '{}' for movie ID {}", dateStr, movieId);
            return null;
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
    
    private int calculatePriority(Double popularity) {
        if (popularity == null) return 0;
        return Math.min((int) (popularity * 10), 1000);
    }
    
    // --- TV Series Processing ---
    
    @Bean
    public Step tvDetailStep() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2);
        
        return new StepBuilder("tvDetailStep", jobRepository)
                .<WorkQueueRow, TvDelta>chunk(20, transactionManager)  // FK 위반 디버깅을 위해 chunk 크기 축소
                .reader(tvWorkQueueReader())
                .processor(tvDetailProcessor())
                .writer(tvSyncWriter)
                .faultTolerant()
                .retry(TmdbClientException.class)
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)
                .skip(Exception.class)
                .skipLimit(1000)
                .taskExecutor(batchTaskExecutor())
                .build();
    }
    
    @Bean
    @StepScope
    public JdbcCursorItemReader<WorkQueueRow> tvWorkQueueReader() {
        log.info("Creating JdbcCursorItemReader for tvWorkQueueReader");
        
        String sql = """
            SELECT entity_type, tmdb_id, priority, processed
            FROM tmdb_work_queue
            WHERE UPPER(entity_type) = 'TV'
              AND processed = false
            ORDER BY priority DESC, tmdb_id ASC
            LIMIT 10000
        """;
        
        return new JdbcCursorItemReaderBuilder<WorkQueueRow>()
                .name("tvWorkQueueReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    WorkQueueRow row = new WorkQueueRow(
                        rs.getString("entity_type"),
                        rs.getLong("tmdb_id"),
                        rs.getInt("priority"),
                        rs.getBoolean("processed")
                    );
                    log.debug("Read TV row: {}", row);
                    return row;
                })
                .fetchSize(100)
                .verifyCursorPosition(false)
                .build();
    }
    
    @Bean
    @StepScope
    public ItemProcessor<WorkQueueRow, TvDelta> tvDetailProcessor() {
        return workItem -> {
            log.info("Processing TV WorkQueueRow: tmdbId={}", workItem.tmdbId());
            
            if (!"TV".equalsIgnoreCase(workItem.entityType())) {
                log.debug("Skipping non-TV entity: type={}, id={}", workItem.entityType(), workItem.tmdbId());
                return null;
            }
            
            Long tvId = workItem.tmdbId();
            
            try {
                TmdbTvSeriesDetailResponse response = tmdbApiClient.getTvDetailForBatch(tvId);
                if (response == null) {
                    log.warn("TV Series ID {} not found or failed to fetch.", tvId);
                    return null;
                }
                
                return TvDelta.fromTmdbResponse(response);
            } catch (TmdbClientException e) {
                log.error("Failed to fetch TV series {}: {}", tvId, e.getMessage());
                throw e;
            }
        };
    }
    
    // --- Person Processing ---
    
    @Bean
    public Step personDetailStep() {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2);
        
        return new StepBuilder("personDetailStep", jobRepository)
                .<WorkQueueRow, PersonDelta>chunk(20, transactionManager)  // FK 위반 디버깅을 위해 chunk 크기 축소
                .reader(personWorkQueueReader())
                .processor(personDetailProcessor())
                .writer(personSyncWriter)
                .faultTolerant()
                .retry(TmdbClientException.class)
                .retryLimit(3)
                .backOffPolicy(backOffPolicy)
                .skip(Exception.class)
                .skipLimit(1000)
                .taskExecutor(batchTaskExecutor())
                .build();
    }
    
    @Bean
    @StepScope
    public JdbcCursorItemReader<WorkQueueRow> personWorkQueueReader() {
        log.info("Creating JdbcCursorItemReader for personWorkQueueReader");
        
        String sql = """
            SELECT entity_type, tmdb_id, priority, processed
            FROM tmdb_work_queue
            WHERE UPPER(entity_type) = 'PERSON'
              AND processed = false
            ORDER BY priority DESC, tmdb_id ASC
            LIMIT 10000
        """;
        
        return new JdbcCursorItemReaderBuilder<WorkQueueRow>()
                .name("personWorkQueueReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper((rs, rowNum) -> {
                    WorkQueueRow row = new WorkQueueRow(
                        rs.getString("entity_type"),
                        rs.getLong("tmdb_id"),
                        rs.getInt("priority"),
                        rs.getBoolean("processed")
                    );
                    log.debug("Read Person row: {}", row);
                    return row;
                })
                .fetchSize(100)
                .verifyCursorPosition(false)
                .build();
    }
    
    @Bean
    @StepScope
    public ItemProcessor<WorkQueueRow, PersonDelta> personDetailProcessor() {
        return workItem -> {
            log.info("Processing Person WorkQueueRow: tmdbId={}", workItem.tmdbId());
            
            if (!"PERSON".equalsIgnoreCase(workItem.entityType())) {
                log.debug("Skipping non-person entity: type={}, id={}", workItem.entityType(), workItem.tmdbId());
                return null;
            }
            
            Long personId = workItem.tmdbId();
            
            try {
                TmdbPersonDetailResponse response = tmdbApiClient.getPersonDetailForBatch(personId);
                if (response == null) {
                    log.warn("Person ID {} not found or failed to fetch.", personId);
                    return null;
                }
                
                return PersonDelta.fromTmdbResponse(response);
            } catch (TmdbClientException e) {
                log.error("Failed to fetch person {}: {}", personId, e.getMessage());
                throw e;
            }
        };
    }
}