package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMediaCreditsResponse;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.datasync.domain.entity.*;
import com.example.CineHive.datasync.domain.service.MovieSyncService;
import com.example.CineHive.datasync.dto.MovieDelta;
import com.example.CineHive.datasync.dto.TmdbExportItem;
import com.example.CineHive.domain.collection.entity.Collection;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
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

    private static final String EXPORT_URL_TEMPLATE = "http://files.tmdb.org/p/exports/%s_ids_%s.json.gz";

    /**
     * 초기 데이터 전체 동기화를 위한 메인 Job.
     * 영화 ID 시딩 -> 영화 상세 정보 수집 순서로 진행됩니다.
     */
    @Bean("initFullSyncJob")
    public Job initFullSyncJob(Step movieExportsSeedStep, Step movieDetailStep) {
        return new JobBuilder("initFullSyncJob", jobRepository)
                .start(movieExportsSeedStep)
                .next(movieDetailStep)
                // .next(tvExportsSeedStep) // TODO: TV, Person 스텝 추가
                .build();
    }

    // --- Seed Step: TMDB Daily Export 파일에서 ID를 읽어 큐에 저장 ---

    @Bean
    @StepScope
    public Step movieExportsSeedStep(JsonItemReader<TmdbExportItem> movieExportItemReader,
                                     JpaItemWriter<TmdbWorkQueue> workQueueItemWriter) {
        return new StepBuilder("movieExportsSeedStep", jobRepository)
                .<TmdbExportItem, TmdbWorkQueue>chunk(5000, transactionManager)
                .reader(movieExportItemReader)
                .processor(exportItemProcessor("movie"))
                .writer(workQueueItemWriter)
                .faultTolerant().skip(Exception.class).skipLimit(1000)
                .build();
    }

    // --- Detail Step: 큐에서 ID를 읽어 상세 정보를 가져와 DB에 저장 ---

    @Bean
    public Step movieDetailStep(JpaPagingItemReader<TmdbWorkQueue> movieWorkQueueReader,
                                ItemProcessor<TmdbWorkQueue, MovieDelta> movieDetailProcessor,
                                ItemWriter<MovieDelta> movieDetailWriter) {
        return new StepBuilder("movieDetailStep", jobRepository)
                .<TmdbWorkQueue, MovieDelta>chunk(100, transactionManager)
                .reader(movieWorkQueueReader)
                .processor(movieDetailProcessor)
                .writer(movieDetailWriter)
                .faultTolerant().retryLimit(3).retry(IOException.class) // 네트워크 오류는 3번 재시도
                .skipLimit(100).skip(Exception.class) // 그 외 오류는 100번까지 스킵
                .build();
    }

    // --- ItemReaders, Processors, Writers ---

    @Bean
    @StepScope
    public JsonItemReader<TmdbExportItem> movieExportItemReader(@Value("#{jobParameters['fileDate']}") String fileDate) {
        return createExportItemReader("movie", fileDate);
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TmdbWorkQueue> movieWorkQueueReader() {
        return new JpaPagingItemReaderBuilder<TmdbWorkQueue>()
                .name("movieWorkQueueReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT w FROM SyncTmdbWorkQueue w WHERE w.entityType = 'movie' ORDER BY w.priority DESC, w.tmdbId ASC")
                .pageSize(100)
                .build();
    }

    public ItemProcessor<TmdbExportItem, TmdbWorkQueue> exportItemProcessor(String entityType) {
        return item -> item.adult() ? null : TmdbWorkQueue.builder().entityType(entityType).tmdbId(item.id()).priority(calculatePriority(item.popularity())).build();
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

            // --- 날짜 파싱 ---
            LocalDate releaseDate = null;
            if (response.releaseDate() != null && !response.releaseDate().isBlank()) {
                try {
                    releaseDate = LocalDate.parse(response.releaseDate());
                } catch (DateTimeParseException e) {
                    log.warn("Could not parse release_date '{}' for movie ID {}", response.releaseDate(), movieId);
                }
            }

            // --- 1. Movie 본체 엔티티 생성 (모든 필드 매핑) ---
            Movie movie = Movie.builder()
                    .tmdbId(response.id())
                    .title(response.title())
                    .originalTitle(response.originalTitle())
                    .overview(response.overview())
                    .tagline(response.tagline()) // 추가
                    .releaseDate(releaseDate)
                    .runtime(response.runtime())
                    .status(response.status())
                    .budget(response.budget()) // 추가
                    .revenue(response.revenue()) // 추가
                    .posterPath(response.posterPath())
                    .backdropPath(response.backdropPath())
                    .popularity(response.popularity() != null ? BigDecimal.valueOf(response.popularity()) : null)
                    .voteAverage(response.voteAverage() != null ? BigDecimal.valueOf(response.voteAverage()) : null)
                    .voteCount(response.voteCount())
                    .collectionId(response.collection() != null ? response.collection().id() : null) // 추가
                    .updatedFromTmdbAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .build();

            // --- 2. 관계 엔티티들 생성 ---
            List<MovieGenre> genres = Optional.ofNullable(response.genres()).orElse(Collections.emptyList()).stream()
                    .map(g -> MovieGenre.builder().movieId(movieId).genreId(g.id().longValue()).build()).toList();

            List<MovieKeyword> keywords = Optional.ofNullable(response.keywords()).map(TmdbKeywordsResponse::getUnifiedKeywords).orElse(Collections.emptyList()).stream()
                    .map(k -> MovieKeyword.builder().movieId(movieId).keywordId(k.id()).build()).toList();

            List<MovieCast> cast = Optional.ofNullable(response.credits()).map(TmdbMediaCreditsResponse::cast).orElse(Collections.emptyList()).stream()
                    .map(c -> MovieCast.builder().creditId(c.creditId()).movieId(movieId).personId(c.id()).characterName(c.character()).castOrder(c.order()).build()).toList();

            List<MovieCrew> crew = Optional.ofNullable(response.credits()).map(TmdbMediaCreditsResponse::crew).orElse(Collections.emptyList()).stream()
                    .map(c -> MovieCrew.builder().creditId(c.creditId()).movieId(movieId).personId(c.id()).job(c.job()).department(c.department()).build()).toList();

            // [추가] Collection, ProductionCompany 등 새로운 엔티티 생성 로직
            Collection collection = Optional.ofNullable(response.collection())
                    .map(c -> Collection.builder().tmdbId(c.id()).name(c.name()).posterPath(c.posterPath()).backdropPath(c.backdropPath()).build())
                    .orElse(null);

            List<ProductionCompany> companies = Optional.ofNullable(response.productionCompanies()).orElse(Collections.emptyList()).stream()
                    .map(c -> ProductionCompany.builder().tmdbId(c.id()).name(c.name()).logoPath(c.logoPath()).originCountry(c.originCountry()).build())
                    .toList();

            List<MovieProductionCompany> movieCompanies = companies.stream()
                    .map(c -> MovieProductionCompany.builder().movieId(movieId).companyId(c.getTmdbId()).build())
                    .toList();

            return new MovieDelta(movie, genres, keywords, cast, crew, collection, companies, movieCompanies);
        };
    }

    @Bean
    public JpaItemWriter<TmdbWorkQueue> workQueueItemWriter() {
        JpaItemWriter<TmdbWorkQueue> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public ItemWriter<MovieDelta> movieDetailWriter() {
        return chunk -> {
            log.info("Writing a chunk of {} movies.", chunk.getItems().size());
            for (MovieDelta delta : chunk.getItems()) {
                movieSyncService.syncMovie(delta);
            }
        };
    }

    // --- Helper Methods & Classes ---

    private JsonItemReader<TmdbExportItem> createExportItemReader(String entityType, String fileDate) {
        if (fileDate == null) {
            fileDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM_dd_yyyy"));
        }
        String url = String.format(EXPORT_URL_TEMPLATE, entityType, fileDate);
        try {
            return new JsonItemReaderBuilder<TmdbExportItem>()
                    .name(entityType + "ExportReader")
                    .resource(new UrlResource(url))
                    .jsonObjectReader(new JacksonJsonObjectReader<>(TmdbExportItem.class))
                    .build();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    private int calculatePriority(double popularity) {
        if (popularity > 100) return 10;
        if (popularity > 50) return 5;
        return 0;
    }
}