package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.datasync.batch.tasklet.ExportDownloadTasklet;
import com.example.CineHive.datasync.batch.writer.MovieSyncWriter;
import com.example.CineHive.datasync.batch.writer.TmdbExportWriter;
import com.example.CineHive.datasync.domain.entity.*;
import com.example.CineHive.datasync.domain.service.MovieSyncService;
import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.dto.TmdbExportItem;
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
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.batch.retry.policy.SimpleRetryPolicy;
import org.springframework.batch.retry.policy.TimeoutRetryPolicy;
import org.springframework.batch.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitFullSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final TmdbApiClient tmdbApiClient;
    private final MovieSyncService movieSyncService;
    private final ExportDownloadTasklet exportDownloadTasklet;
    private final TmdbExportWriter tmdbExportWriter;
    private final MovieSyncWriter movieSyncWriter;

    private static final String EXPORT_URL_TEMPLATE = "http://files.tmdb.org/p/exports/%s_ids_%s.json.gz";

    @Bean("initFullSyncJob")
    public Job initFullSyncJob() {
        return new JobBuilder("initFullSyncJob", jobRepository)
                .start(downloadExportStep())
                .next(seedWorkQueueStep())
                .next(movieDetailStep())
                .build();
    }

    @Bean
    public Step downloadExportStep() {
        return new StepBuilder("downloadExportStep", jobRepository)
                .tasklet(exportDownloadTasklet, transactionManager)
                .build();
    }
    
    @Bean
    public Step seedWorkQueueStep() {
        return new StepBuilder("seedWorkQueueStep", jobRepository)
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
                .<TmdbWorkQueue, MovieDelta>chunk(100, transactionManager)
                .reader(movieWorkQueueReader())
                .processor(movieDetailProcessor())
                .writer(movieSyncWriter)
                .taskExecutor(batchTaskExecutor())
                .throttleLimit(8)  // TMDB rate limit consideration
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
    public JsonItemReader<TmdbExportItem> exportReader(
            @Value("#{stepExecutionContext['exportPath']}") String exportPath) {
        if (exportPath == null) {
            // Fallback for testing
            return new JsonItemReaderBuilder<TmdbExportItem>()
                    .name("exportReader")
                    .resource(new FileSystemResource("/data/exports/movie_export.json"))
                    .jsonObjectReader(new JacksonJsonObjectReader<>(TmdbExportItem.class))
                    .build();
        }
        
        return new JsonItemReaderBuilder<TmdbExportItem>()
                .name("exportReader")
                .resource(new FileSystemResource(exportPath))
                .jsonObjectReader(new JacksonJsonObjectReader<>(TmdbExportItem.class))
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TmdbWorkQueue> movieWorkQueueReader() {
        return new JpaPagingItemReaderBuilder<TmdbWorkQueue>()
                .name("movieWorkQueueReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT w FROM TmdbWorkQueue w WHERE w.entityType = 'movie' AND w.status = 'READY' ORDER BY w.priority DESC, w.tmdbId ASC")
                .pageSize(100)
                .build();
    }

    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public ItemProcessor<TmdbWorkQueue, MovieDelta> movieDetailProcessor() {
        return workItem -> {
            Long movieId = workItem.getTmdbId();
            log.info("Processing Movie ID: {}", movieId);

            TmdbMovieDetailResponse response = tmdbApiClient.getMovieDetail(movieId);
            if (response == null) {
                log.warn("Movie ID {} not found or failed to fetch.", movieId);
                return null;
            }

            LocalDate releaseDate = parseDate(response.releaseDate(), movieId);

            Movie movie = Movie.builder()
                    .tmdbId(response.id()).title(response.title()).originalTitle(response.originalTitle())
                    .overview(response.overview()).tagline(response.tagline()).releaseDate(releaseDate)
                    .runtime(response.runtime()).status(response.status()).budget(response.budget())
                    .revenue(response.revenue()).posterPath(response.posterPath()).backdropPath(response.backdropPath())
                    .popularity(toBigDecimal(response.popularity())).voteAverage(toBigDecimal(response.voteAverage())).voteCount(response.voteCount())
                    .collectionId(Optional.ofNullable(response.collection()).map(TmdbCollectionResponse::id).orElse(null))
                    .updatedFromTmdbAt(ZonedDateTime.now(ZoneOffset.UTC)).build();

            List<MovieGenre> genres = Optional.ofNullable(response.genres()).orElse(Collections.emptyList()).stream()
                    .map(g -> MovieGenre.builder().movieId(movieId).genreId(g.id().longValue()).build()).toList();

            List<MovieKeyword> keywords = Optional.ofNullable(response.keywords()).map(TmdbKeywordsResponse::getUnifiedKeywords).orElse(Collections.emptyList()).stream()
                    .map(k -> MovieKeyword.builder().movieId(movieId).keywordId(k.id()).build()).toList();

            List<MovieCast> cast = Optional.ofNullable(response.credits()).map(TmdbMediaCreditsResponse::cast).orElse(Collections.emptyList()).stream()
                    .map(c -> MovieCast.builder()
                            .creditId(c.creditId())
                            .movieId(movieId)
                            .personId(c.id())
                            .characterName(c.character())
                            .castOrder(c.order())
                            .build()
                    ).toList();

            List<MovieCrew> crew = Optional.ofNullable(response.credits()).map(TmdbMediaCreditsResponse::crew).orElse(Collections.emptyList()).stream()
                    .map(c -> MovieCrew.builder()
                            .creditId(c.creditId())
                            .movieId(movieId)
                            .personId(c.id())
                            .job(c.job())
                            .department(c.department())
                            .build()
                    ).toList();

            Collection collection = Optional.ofNullable(response.collection())
                    .map(c -> Collection.builder().tmdbId(c.id()).name(c.name()).posterPath(c.posterPath()).backdropPath(c.backdropPath()).build()).orElse(null);

            List<ProductionCompany> companies = Optional.ofNullable(response.productionCompanies()).orElse(Collections.emptyList()).stream()
                    .map(c -> ProductionCompany.builder().tmdbId(c.id()).name(c.name()).logoPath(c.logoPath()).originCountry(c.originCountry()).build()).toList();

            List<MovieProductionCompany> movieCompanies = companies.stream()
                    .map(c -> MovieProductionCompany.builder().movieId(movieId).companyId(c.getTmdbId()).build()).toList();

            return new MovieDelta(movie, collection, companies, genres, keywords, cast, crew, movieCompanies);
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
}