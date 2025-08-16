package com.example.CineHive.datasync.batch;

import com.example.CineHive.datasync.batch.reader.OptimizedWorkQueueReader;
import com.example.CineHive.datasync.batch.writer.OptimizedMovieSyncWriter;
import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.dto.WorkQueueRow;
import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 최적화된 배치 설정
 * - FOR UPDATE SKIP LOCKED 기반 Reader
 * - JDBC Upsert 기반 Writer
 * - 단일 스레드 처리로 데드락 방지
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class OptimizedBatchConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final TmdbApiClient tmdbApiClient;
    
    @Bean
    public Job optimizedMovieSyncJob() {
        return new JobBuilder("optimizedMovieSyncJob", jobRepository)
                .start(optimizedMovieDetailStep())
                .build();
    }
    
    @Bean
    public Step optimizedMovieDetailStep() {
        return new StepBuilder("optimizedMovieDetailStep", jobRepository)
                .<WorkQueueRow, MovieDelta>chunk(20, transactionManager)
                .reader(optimizedMovieQueueReader(null))
                .processor(optimizedMovieProcessor())
                .writer(optimizedMovieSyncWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .taskExecutor(singleThreadExecutor()) // 단일 스레드로 데드락 방지
                .build();
    }
    
    @Bean
    @StepScope
    public OptimizedWorkQueueReader optimizedMovieQueueReader(
            @Value("#{jobParameters['batchSize'] ?: 50}") Integer batchSize) {
        return new OptimizedWorkQueueReader(dataSource, "MOVIE", batchSize);
    }
    
    @Bean
    @StepScope
    public ItemProcessor<WorkQueueRow, MovieDelta> optimizedMovieProcessor() {
        return workItem -> {
            Long movieId = workItem.tmdbId();
            log.debug("Processing movie: {}", movieId);
            
            try {
                TmdbMovieDetailResponse response = tmdbApiClient.getMovieDetail(movieId);
                if (response == null) {
                    log.warn("Movie {} not found in TMDB", movieId);
                    return null;
                }
                
                return MovieDelta.fromTmdbResponse(response);
            } catch (Exception e) {
                log.error("Error fetching movie {}: {}", movieId, e.getMessage());
                throw e;
            }
        };
    }
    
    @Bean
    public OptimizedMovieSyncWriter optimizedMovieSyncWriter() {
        return new OptimizedMovieSyncWriter(dataSource);
    }
    
    @Bean
    public TaskExecutor singleThreadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);  // 단일 스레드
        executor.setMaxPoolSize(1);   // 단일 스레드
        executor.setQueueCapacity(0); // 큐 사용 안함
        executor.setThreadNamePrefix("batch-single-");
        executor.initialize();
        return executor;
    }
}