package com.example.CineHive.batch.config;

import com.example.CineHive.batch.common.BatchJobExecutionFinder;
import com.example.CineHive.batch.common.LoggingSkipListener;
import com.example.CineHive.batch.common.TmdbApiSkipPolicy;
import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbChangeItemResponse;
import com.example.CineHive.client.tmdb.dto.TmdbChangesResponse;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse;
import com.example.CineHive.domain.search.document.MediaDocument;
import com.example.CineHive.domain.search.repository.MediaDocumentRepository;
import com.example.CineHive.global.exception.TmdbClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbIndexingJobConfig {

    private final TmdbApiClient tmdbApiClient;
    private final MediaDocumentRepository mediaDocumentRepository;
    private final BatchJobExecutionFinder jobExecutionFinder;
    private static final int CHUNK_SIZE = 100;
    private static final String JOB_NAME = "tmdbMediaIndexingJob";

    @Bean
    public Job tmdbMediaIndexingJob(JobRepository jobRepository,
                                    @Qualifier("movieChangesIndexingStep") Step movieChangesIndexingStep,
                                    @Qualifier("tvChangesIndexingStep") Step tvChangesIndexingStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(movieChangesIndexingStep)
                .next(tvChangesIndexingStep)
                .build();
    }

    @Bean
    public Step movieChangesIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                         ItemReader<Long> movieChangesItemReader,
                                         ItemProcessor<Long, MediaDocument> movieDetailProcessor,
                                         ItemWriter<MediaDocument> elasticsearchItemWriter) {
        return new StepBuilder("movieChangesIndexingStep", jobRepository)
                .<Long, MediaDocument>chunk(CHUNK_SIZE, tm)
                .reader(movieChangesItemReader)
                .processor(movieDetailProcessor)
                .writer(elasticsearchItemWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(TmdbClientException.class)
                .skipPolicy(new TmdbApiSkipPolicy())
                .skipLimit(50)
                .listener(new LoggingSkipListener<>())
                .build();
    }

    @Bean
    public Step tvChangesIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                      ItemReader<Long> tvChangesItemReader,
                                      ItemProcessor<Long, MediaDocument> tvDetailProcessor,
                                      ItemWriter<MediaDocument> elasticsearchItemWriter) {
        return new StepBuilder("tvChangesIndexingStep", jobRepository)
                .<Long, MediaDocument>chunk(CHUNK_SIZE, tm)
                .reader(tvChangesItemReader)
                .processor(tvDetailProcessor)
                .writer(elasticsearchItemWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(TmdbClientException.class)
                .skipPolicy(new TmdbApiSkipPolicy())
                .skipLimit(50)
                .listener(new LoggingSkipListener<>())
                .build();
    }

    // --- Readers (StepScope로 동적 생성) ---
    @Bean
    @StepScope
    public ItemReader<Long> movieChangesItemReader() {
        return createChangesItemReader(tmdbApiClient::getMovieChanges);
    }

    @Bean
    @StepScope
    public ItemReader<Long> tvChangesItemReader() {
        return createChangesItemReader(tmdbApiClient::getTvChanges);
    }

    // --- Processors ---
    @Bean
    public ItemProcessor<Long, MediaDocument> movieDetailProcessor() {
        // try-catch를 제거하여 예외가 Step으로 전파되도록 함
        return movieId -> {
            TmdbMovieDetailResponse movieDetail = tmdbApiClient.getMovieDetail(movieId);
            return MediaDocument.from(movieDetail);
        };
    }

    @Bean
    public ItemProcessor<Long, MediaDocument> tvDetailProcessor() {
        // try-catch를 제거하여 예외가 Step으로 전파되도록 함
        return tvId -> {
            TmdbTvSeriesDetailResponse tvDetail = tmdbApiClient.getTvSeriesDetail(tvId);
            return MediaDocument.from(tvDetail);
        };
    }

    // --- Writer (공통 사용) ---
    @Bean
    public ItemWriter<MediaDocument> elasticsearchItemWriter() {
        return items -> {
            if (!items.isEmpty()) mediaDocumentRepository.saveAll(items);
        };
    }

    // --- Helper Method for Readers ---
    private ListItemReader<Long> createChangesItemReader(ChangesApiLoader apiLoader) {
        LocalDateTime lastSuccessTime = jobExecutionFinder.findLastSuccessfulJobEndTime(JOB_NAME)
                .orElse(LocalDateTime.now().minusDays(1));

        String startDate = lastSuccessTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<Long> changedIds = new ArrayList<>();
        int currentPage = 1;
        int totalPages;

        do {
            TmdbChangesResponse response = apiLoader.load(startDate, currentPage++);
            if (response == null || response.results() == null) break;

            response.results().stream()
                    .map(TmdbChangeItemResponse::id)
                    .forEach(changedIds::add);
            totalPages = response.totalPages();
        } while (currentPage <= totalPages);

        log.info("Total changed IDs found since {}: {}", startDate, changedIds.size());
        return new ListItemReader<>(changedIds);
    }

    @FunctionalInterface
    private interface ChangesApiLoader {
        TmdbChangesResponse load(String startDate, int page);
    }
}