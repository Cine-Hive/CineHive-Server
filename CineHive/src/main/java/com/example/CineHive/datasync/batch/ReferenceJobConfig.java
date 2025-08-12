package com.example.CineHive.datasync.batch;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbConfigurationResponse;
import com.example.CineHive.datasync.domain.entity.TmdbConfiguration;
import com.example.CineHive.datasync.domain.entity.TmdbImageSize;
import com.example.CineHive.datasync.domain.repository.TmdbConfigurationRepository;
import com.example.CineHive.datasync.domain.repository.TmdbImageSizeRepository;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReferenceJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TmdbApiClient tmdbApiClient;
    private final TmdbConfigurationRepository tmdbConfigurationRepository;
    private final TmdbImageSizeRepository tmdbImageSizeRepository;

    @Bean
    public Job referenceJob() {
        return new JobBuilder("referenceJob", jobRepository)
                .start(fetchConfigurationStep())
                // .next(fetchCountriesStep()) // 추후 국가, 언어 등 다른 Step 추가 가능
                .build();
    }

    @Bean
    public Step fetchConfigurationStep() {
        return new StepBuilder("fetchConfigurationStep", jobRepository)
                .<TmdbConfigurationResponse, ProcessedConfiguration>chunk(1, transactionManager)
                .reader(configurationReader())
                .processor(configurationProcessor())
                .writer(configurationWriter())
                .build();
    }

    @Bean
    public ItemReader<TmdbConfigurationResponse> configurationReader() {
        return new ItemReader<>() {
            private boolean hasBeenRead = false;
            @Override
            public TmdbConfigurationResponse read() {
                if (hasBeenRead) {
                    return null; // 배치 종료 신호
                }
                log.info("Fetching TMDB API configuration...");
                hasBeenRead = true;
                return tmdbApiClient.getConfiguration();
            }
        };
    }

    @Bean
    public ItemProcessor<TmdbConfigurationResponse, ProcessedConfiguration> configurationProcessor() {
        return response -> {
            log.info("Processing TMDB API configuration...");
            // 1. 메인 Configuration 엔티티 생성
            TmdbConfiguration configEntity = TmdbConfiguration.builder()
                    .secureBaseUrl(response.images().secureBaseUrl())
                    .baseUrl(response.images().baseUrl())
                    .fetchedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .build();

            // 2. 모든 종류의 이미지 사이즈를 하나의 리스트로 통합
            List<TmdbImageSize> allImageSizes = new ArrayList<>();
            addImageSizes(allImageSizes, "poster", response.images().posterSizes());
            addImageSizes(allImageSizes, "backdrop", response.images().backdropSizes());
            addImageSizes(allImageSizes, "still", response.images().stillSizes());
            addImageSizes(allImageSizes, "profile", response.images().profileSizes());
            addImageSizes(allImageSizes, "logo", response.images().logoSizes());

            return new ProcessedConfiguration(configEntity, allImageSizes);
        };
    }

    private void addImageSizes(List<TmdbImageSize> targetList, String kind, List<String> sizes) {
        if (sizes == null) return;
        AtomicInteger order = new AtomicInteger(0);
        sizes.forEach(sizeCode -> targetList.add(TmdbImageSize.builder()
                .kind(kind)
                .sizeCode(sizeCode)
                .orderNo(order.getAndIncrement())
                .build()));
    }

    @Bean
    public ItemWriter<ProcessedConfiguration> configurationWriter() {
        return chunk -> {
            log.info("Writing TMDB API configuration to DB...");
            for (ProcessedConfiguration item : chunk.getItems()) {
                // 1. 메인 Configuration 저장 (ID가 1이므로 항상 UPSERT)
                tmdbConfigurationRepository.save(item.configuration);

                // 2. 이미지 사이즈는 매번 전체를 교체 (기존 것 모두 삭제 후 새로 삽입)
                tmdbImageSizeRepository.deleteAllInBatch();
                tmdbImageSizeRepository.saveAll(item.imageSizes);

                log.info("Successfully updated TMDB configuration and {} image sizes.", item.imageSizes.size());
            }
        };
    }

    // Processor에서 Writer로 데이터를 전달하기 위한 래퍼 클래스
    private record ProcessedConfiguration(TmdbConfiguration configuration, List<TmdbImageSize> imageSizes) {}
}