package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.datasync.dto.MovieDelta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 고성능 배치 설정
 * - 병렬 처리로 처리 속도 대폭 향상
 * - FK 제약 조건 일시 비활성화로 트랜잭션 문제 해결
 * - 대량 Bulk Insert로 성능 최적화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class HighPerformanceBatchConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final TmdbApiClient tmdbApiClient;
    
    @Bean
    public Job highPerformanceMovieSyncJob() {
        return new JobBuilder("highPerformanceMovieSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(prepareStep())
                .next(processMovieStep())
                .next(cleanupStep())
                .build();
    }
    
    @Bean
    public Step prepareStep() {
        return new StepBuilder("prepareStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                    
                    // FK 제약 조건 일시 비활성화
                    log.info("Disabling FK constraints for batch processing...");
                    jdbc.execute("SET session_replication_role = 'replica';");
                    
                    // 처리할 항목 수 확인
                    Integer pendingCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM tmdb_work_queue WHERE entity_type='MOVIE' AND status='PENDING'",
                        Integer.class
                    );
                    log.info("Found {} pending movies to process", pendingCount);
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step processMovieStep() {
        return new StepBuilder("processMovieStep", jobRepository)
                .<Long, MovieDelta>chunk(200, transactionManager) // 청크 크기 대폭 증가
                .reader(highPerformanceReader())
                .processor(parallelProcessor())
                .writer(bulkWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000) // 스킵 한도 증가
                .taskExecutor(multiThreadExecutor()) // 멀티스레드 처리
                .build();
    }
    
    @Bean
    public Step cleanupStep() {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                    
                    // FK 제약 조건 재활성화
                    log.info("Re-enabling FK constraints...");
                    jdbc.execute("SET session_replication_role = 'origin';");
                    
                    // 처리 결과 통계
                    Map<String, Object> stats = jdbc.queryForMap(
                        "SELECT " +
                        "COUNT(CASE WHEN status='DONE' THEN 1 END) as completed, " +
                        "COUNT(CASE WHEN status='FAILED' THEN 1 END) as failed, " +
                        "COUNT(CASE WHEN status='PENDING' THEN 1 END) as pending " +
                        "FROM tmdb_work_queue WHERE entity_type='MOVIE'"
                    );
                    
                    log.info("Batch processing complete - Completed: {}, Failed: {}, Pending: {}", 
                        stats.get("completed"), stats.get("failed"), stats.get("pending"));
                    
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<Long> highPerformanceReader() {
        return new ItemReader<Long>() {
            private final Queue<Long> queue = new ConcurrentLinkedQueue<>();
            private final AtomicInteger batchCount = new AtomicInteger(0);
            private static final int BATCH_SIZE = 1000;
            
            @Override
            public Long read() {
                if (queue.isEmpty() && batchCount.get() < 100) { // 최대 100 배치 (100,000개)
                    loadNextBatch();
                }
                return queue.poll();
            }
            
            private void loadNextBatch() {
                JdbcTemplate jdbc = new JdbcTemplate(dataSource);
                
                // FOR UPDATE SKIP LOCKED로 배치 단위로 처리
                String sql = """
                    WITH batch AS (
                        SELECT tmdb_id
                        FROM tmdb_work_queue
                        WHERE entity_type = 'MOVIE'
                          AND status = 'PENDING'
                        ORDER BY priority DESC, tmdb_id
                        FOR UPDATE SKIP LOCKED
                        LIMIT ?
                    )
                    UPDATE tmdb_work_queue q
                    SET status = 'PROCESSING',
                        updated_at = NOW()
                    FROM batch
                    WHERE q.tmdb_id = batch.tmdb_id
                      AND q.entity_type = 'MOVIE'
                    RETURNING q.tmdb_id
                """;
                
                List<Long> ids = jdbc.queryForList(sql, Long.class, BATCH_SIZE);
                queue.addAll(ids);
                
                if (!ids.isEmpty()) {
                    batchCount.incrementAndGet();
                    log.info("Loaded batch {} with {} movies", batchCount.get(), ids.size());
                }
            }
        };
    }
    
    @Bean
    @StepScope
    public ItemProcessor<Long, MovieDelta> parallelProcessor() {
        return movieId -> {
            try {
                TmdbMovieDetailResponse response = tmdbApiClient.getMovieDetail(movieId);
                if (response == null) {
                    updateStatus(movieId, "FAILED", "Movie not found in TMDB");
                    return null;
                }
                
                MovieDelta delta = MovieDelta.fromTmdbResponse(response);
                return delta;
                
            } catch (Exception e) {
                log.error("Error processing movie {}: {}", movieId, e.getMessage());
                updateStatus(movieId, "FAILED", e.getMessage());
                throw e;
            }
        };
    }
    
    @Bean
    @StepScope
    public ItemWriter<MovieDelta> bulkWriter() {
        return chunk -> {
            if (chunk.isEmpty()) return;
            
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<? extends MovieDelta> items = chunk.getItems();
            
            // 배치 단위로 처리
            processBatch(jdbc, items);
            
            // 성공한 항목들의 상태 업데이트
            String updateSql = "UPDATE tmdb_work_queue SET status = 'DONE', updated_at = NOW() WHERE tmdb_id = ? AND entity_type = 'MOVIE'";
            List<Object[]> updateParams = items.stream()
                .map(delta -> new Object[]{delta.movie().getTmdbId()})
                .collect(Collectors.toList());
            jdbc.batchUpdate(updateSql, updateParams);
            
            log.info("Successfully processed {} movies", items.size());
        };
    }
    
    private void processBatch(JdbcTemplate jdbc, List<? extends MovieDelta> deltas) {
        // 1. Genres 배치 삽입
        Set<Object[]> genreParams = new HashSet<>();
        for (MovieDelta delta : deltas) {
            if (delta.genreEntities() != null) {
                delta.genreEntities().forEach(g -> 
                    genreParams.add(new Object[]{g.getTmdbId(), g.getName()})
                );
            }
        }
        if (!genreParams.isEmpty()) {
            jdbc.batchUpdate(
                "INSERT INTO genre (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                new ArrayList<>(genreParams)
            );
        }
        
        // 2. Keywords 배치 삽입
        Set<Object[]> keywordParams = new HashSet<>();
        for (MovieDelta delta : deltas) {
            if (delta.keywordEntities() != null) {
                delta.keywordEntities().forEach(k -> 
                    keywordParams.add(new Object[]{k.getTmdbId(), k.getName()})
                );
            }
        }
        if (!keywordParams.isEmpty()) {
            jdbc.batchUpdate(
                "INSERT INTO keyword (tmdb_id, name) VALUES (?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                new ArrayList<>(keywordParams)
            );
        }
        
        // 3. Persons 배치 삽입
        Set<Object[]> personParams = new HashSet<>();
        for (MovieDelta delta : deltas) {
            if (delta.persons() != null) {
                delta.persons().forEach(p -> 
                    personParams.add(new Object[]{
                        p.getTmdbId(), 
                        p.getName(), 
                        p.getProfilePath(),
                        Timestamp.from(ZonedDateTime.now().toInstant())
                    })
                );
            }
        }
        if (!personParams.isEmpty()) {
            jdbc.batchUpdate(
                "INSERT INTO person (tmdb_id, name, profile_path, updated_from_tmdb_at, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW()) ON CONFLICT (tmdb_id) DO NOTHING",
                new ArrayList<>(personParams)
            );
        }
        
        // 4. Companies 배치 삽입
        Set<Object[]> companyParams = new HashSet<>();
        for (MovieDelta delta : deltas) {
            if (delta.companies() != null) {
                delta.companies().forEach(c -> 
                    companyParams.add(new Object[]{
                        c.getTmdbId(), 
                        c.getName(), 
                        c.getLogoPath(), 
                        c.getOriginCountry()
                    })
                );
            }
        }
        if (!companyParams.isEmpty()) {
            jdbc.batchUpdate(
                "INSERT INTO production_company (tmdb_id, name, logo_path, origin_country) " +
                "VALUES (?, ?, ?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                new ArrayList<>(companyParams)
            );
        }
        
        // 5. Collections 배치 삽입
        List<Object[]> collectionParams = new ArrayList<>();
        for (MovieDelta delta : deltas) {
            if (delta.collection() != null) {
                collectionParams.add(new Object[]{
                    delta.collection().getTmdbId(),
                    delta.collection().getName(),
                    delta.collection().getPosterPath(),
                    delta.collection().getBackdropPath()
                });
            }
        }
        if (!collectionParams.isEmpty()) {
            jdbc.batchUpdate(
                "INSERT INTO collection (tmdb_id, name, poster_path, backdrop_path) " +
                "VALUES (?, ?, ?, ?) ON CONFLICT (tmdb_id) DO NOTHING",
                collectionParams
            );
        }
        
        // 6. Movies 배치 삽입
        List<Object[]> movieParams = deltas.stream().map(delta -> {
            var m = delta.movie();
            return new Object[]{
                m.getTmdbId(),
                m.getTitle(),
                m.getOriginalTitle(),
                m.getOverview(),
                m.getReleaseDate() != null ? java.sql.Date.valueOf(m.getReleaseDate()) : null,
                m.getRuntime(),
                m.getVoteAverage(),
                m.getVoteCount(),
                m.getPopularity(),
                m.getPosterPath(),
                m.getBackdropPath(),
                m.getStatus(),
                m.getTagline(),
                m.getBudget(),
                m.getRevenue(),
                m.getCollectionId(),
                Timestamp.from(m.getUpdatedFromTmdbAt().toInstant())
            };
        }).collect(Collectors.toList());
        
        jdbc.batchUpdate(
            "INSERT INTO movie (tmdb_id, title, original_title, overview, release_date, runtime, " +
            "vote_average, vote_count, popularity, poster_path, backdrop_path, status, tagline, " +
            "budget, revenue, collection_id, updated_from_tmdb_at, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) " +
            "ON CONFLICT (tmdb_id) DO UPDATE SET " +
            "title = EXCLUDED.title, original_title = EXCLUDED.original_title, overview = EXCLUDED.overview, " +
            "release_date = EXCLUDED.release_date, runtime = EXCLUDED.runtime, vote_average = EXCLUDED.vote_average, " +
            "vote_count = EXCLUDED.vote_count, popularity = EXCLUDED.popularity, poster_path = EXCLUDED.poster_path, " +
            "backdrop_path = EXCLUDED.backdrop_path, status = EXCLUDED.status, tagline = EXCLUDED.tagline, " +
            "budget = EXCLUDED.budget, revenue = EXCLUDED.revenue, collection_id = EXCLUDED.collection_id, " +
            "updated_from_tmdb_at = EXCLUDED.updated_from_tmdb_at, updated_at = NOW()",
            movieParams
        );
        
        // 7. 관계 테이블들 배치 삽입 (movie_genre, movie_keyword, movie_cast, movie_crew, movie_production_company)
        // 간단히 하기 위해 DELETE + INSERT 방식 사용
        for (MovieDelta delta : deltas) {
            Long movieId = delta.movie().getTmdbId();
            
            // Movie Genres
            if (delta.genres() != null && !delta.genres().isEmpty()) {
                jdbc.update("DELETE FROM movie_genre WHERE movie_id = ?", movieId);
                List<Object[]> mgParams = delta.genres().stream()
                    .map(mg -> new Object[]{movieId, mg.getGenreId()})
                    .collect(Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO movie_genre (movie_id, genre_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                    mgParams
                );
            }
            
            // Movie Keywords
            if (delta.keywords() != null && !delta.keywords().isEmpty()) {
                jdbc.update("DELETE FROM movie_keyword WHERE movie_id = ?", movieId);
                List<Object[]> mkParams = delta.keywords().stream()
                    .map(mk -> new Object[]{movieId, mk.getKeywordId()})
                    .collect(Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO movie_keyword (movie_id, keyword_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                    mkParams
                );
            }
            
            // Movie Cast
            if (delta.cast() != null && !delta.cast().isEmpty()) {
                jdbc.update("DELETE FROM movie_cast WHERE movie_id = ?", movieId);
                List<Object[]> mcParams = delta.cast().stream()
                    .map(mc -> new Object[]{
                        mc.getCreditId(), movieId, mc.getPersonId(), 
                        mc.getCharacterName(), mc.getCastOrder()
                    })
                    .collect(Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO movie_cast (credit_id, movie_id, person_id, character_name, cast_order, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                    mcParams
                );
            }
            
            // Movie Crew
            if (delta.crew() != null && !delta.crew().isEmpty()) {
                jdbc.update("DELETE FROM movie_crew WHERE movie_id = ?", movieId);
                List<Object[]> mcrParams = delta.crew().stream()
                    .map(mcr -> new Object[]{
                        mcr.getCreditId(), movieId, mcr.getPersonId(),
                        mcr.getDepartment(), mcr.getJob()
                    })
                    .collect(Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO movie_crew (credit_id, movie_id, person_id, department, job, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, NOW(), NOW())",
                    mcrParams
                );
            }
            
            // Movie Production Companies
            if (delta.movieCompanies() != null && !delta.movieCompanies().isEmpty()) {
                jdbc.update("DELETE FROM movie_production_company WHERE movie_id = ?", movieId);
                List<Object[]> mpcParams = delta.movieCompanies().stream()
                    .map(mpc -> new Object[]{movieId, mpc.getCompanyId()})
                    .collect(Collectors.toList());
                jdbc.batchUpdate(
                    "INSERT INTO movie_production_company (movie_id, company_id, created_at, updated_at) VALUES (?, ?, NOW(), NOW())",
                    mpcParams
                );
            }
        }
    }
    
    private void updateStatus(Long tmdbId, String status, String error) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update(
            "UPDATE tmdb_work_queue SET status = ?, last_error = ?, updated_at = NOW() WHERE tmdb_id = ? AND entity_type = 'MOVIE'",
            status, error, tmdbId
        );
    }
    
    @Bean
    public TaskExecutor multiThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);     // 코어 스레드 10개
        executor.setMaxPoolSize(20);      // 최대 스레드 20개
        executor.setQueueCapacity(100);   // 큐 용량
        executor.setThreadNamePrefix("batch-parallel-");
        executor.initialize();
        return executor;
    }
}