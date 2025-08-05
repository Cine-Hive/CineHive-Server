package com.example.CineHive.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.client.tmdb.dto.TmdbMovieResponse;
import com.example.CineHive.domain.search.document.MediaDocument;
import com.example.CineHive.domain.search.repository.MediaDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbIndexingJobConfig {

    private final TmdbApiClient tmdbApiClient;
    private final MediaDocumentRepository mediaDocumentRepository;

    private static final int CHUNK_SIZE = 100;

    /**
     * TMDB 미디어 데이터를 Elasticsearch에 색인하는 전체 Job을 정의합니다.
     */
    @Bean
    public Job tmdbMediaIndexingJob(JobRepository jobRepository, Step movieIndexingStep) {
        return new JobBuilder("tmdbMediaIndexingJob", jobRepository)
                .start(movieIndexingStep)
                // TODO: TV 시리즈 색인 Step 추가
                .build();
    }

    /**
     * 영화 데이터를 읽고, 처리하고, 쓰는 단일 Step을 정의합니다.
     */
    @Bean
    public Step movieIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                  ItemReader<TmdbMovieResponse> movieItemReader,
                                  ItemProcessor<TmdbMovieResponse, MediaDocument> movieItemProcessor,
                                  ItemWriter<MediaDocument> elasticsearchItemWriter) {
        return new StepBuilder("movieIndexingStep", jobRepository)
                .<TmdbMovieResponse, MediaDocument>chunk(CHUNK_SIZE, tm)
                .reader(movieItemReader)
                .processor(movieItemProcessor)
                .writer(elasticsearchItemWriter)
                .build();
    }

    /**
     * [Reader] TMDB API에서 인기 영화 목록을 읽어옵니다.
     * 실제 운영에서는 /changes API를 사용하도록 개선해야 합니다.
     */
    @Bean
    public ItemReader<TmdbMovieResponse> movieItemReader() {
        log.info("TMDB 인기 영화 목록 조회를 시작합니다 (Batch Reader).");
        // 우선 1페이지만 읽어오는 간단한 리더로 구현
        List<TmdbMovieResponse> popularMovies = tmdbApiClient.getPopularMovies(1).getResults();
        return new ListItemReader<>(popularMovies);
    }

    /**
     * [Processor] 읽어온 영화 정보를 MediaDocument로 변환합니다.
     */
    @Bean
    public ItemProcessor<TmdbMovieResponse, MediaDocument> movieItemProcessor() {
        return movieSummary -> {
            log.debug("Processing movie ID: {}", movieSummary.id());
            try {
                // 요약 정보만으로는 부족하므로, 상세 정보를 다시 조회
                TmdbMovieDetailResponse movieDetail = tmdbApiClient.getMovieDetail(movieSummary.id());
                return MediaDocument.from(movieDetail);
            } catch (Exception e) {
                log.error("영화 ID {} 처리 중 오류 발생: {}", movieSummary.id(), e.getMessage());
                return null; // 실패한 아이템은 건너뜀
            }
        };
    }

    /**
     * [Writer] 변환된 MediaDocument를 Elasticsearch에 대량 저장합니다.
     * Builder 대신 Repository의 saveAll 메서드를 직접 호출하는 람다로 구현합니다.
     */
    @Bean
    public ItemWriter<MediaDocument> elasticsearchItemWriter() {
        return items -> mediaDocumentRepository.saveAll(items);
    }
}