package com.example.CineHive.batch.config;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbChangeItemResponse;
import com.example.CineHive.client.tmdb.dto.TmdbChangesResponse;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse;
import com.example.CineHive.domain.collection.entity.Collection;
import com.example.CineHive.domain.collection.repository.CollectionRepository;
import com.example.CineHive.domain.search.document.MediaDocument;
import com.example.CineHive.domain.search.repository.MediaDocumentRepository;
import com.example.CineHive.global.exception.TmdbClientException;
import com.example.CineHive.batch.common.BatchJobExecutionFinder;
import com.example.CineHive.batch.common.LoggingSkipListener;
import com.example.CineHive.batch.common.TmdbApiSkipPolicy;
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
    private final CollectionRepository collectionRepository;
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

    @Bean
    public ItemProcessor<Long, MediaDocument> movieDetailProcessor() {
        return movieId -> {
            TmdbMovieDetailResponse movieDetail = tmdbApiClient.getMovieDetail(movieId);

            // 영화에 포함된 컬렉션 정보를 우리 DB에 저장
            if (movieDetail.collection() != null) {
                Collection collection = Collection.builder()
                        .id(movieDetail.collection().id())
                        .name(movieDetail.collection().name())
                        .posterPath(movieDetail.collection().posterPath())
                        .backdropPath(movieDetail.collection().backdropPath())
                        .build();
                collectionRepository.save(collection);
            }

            return MediaDocument.from(movieDetail);
        };
    }

    @Bean
    public ItemProcessor<Long, MediaDocument> tvDetailProcessor() {
        return tvId -> {
            TmdbTvSeriesDetailResponse tvDetail = tmdbApiClient.getTvSeriesDetail(tvId);
            return MediaDocument.from(tvDetail);
        };
    }

    @Bean
    public ItemWriter<MediaDocument> elasticsearchItemWriter() {
        return items -> {
            if (!items.isEmpty()) mediaDocumentRepository.saveAll(items);
        };
    }

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
