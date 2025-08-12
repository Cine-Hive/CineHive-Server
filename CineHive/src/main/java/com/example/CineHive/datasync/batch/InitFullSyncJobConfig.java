package com.example.CineHive.datasync.batch;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.dto.TmdbExportItem;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitFullSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private static final String EXPORT_URL_TEMPLATE = "http://files.tmdb.org/p/exports/%s_ids_%s.json.gz";

    @Bean("initFullSyncJob")
    public Job initFullSyncJob(Step movieExportsSeedStep, Step tvExportsSeedStep, Step personExportsSeedStep) {
        return new JobBuilder("initFullSyncJob", jobRepository)
                .start(movieExportsSeedStep)
                .next(tvExportsSeedStep)
                .next(personExportsSeedStep)
                .build();
    }

    @Bean
    @StepScope
    public Step movieExportsSeedStep(JsonItemReader<TmdbExportItem> movieExportItemReader) {
        return new StepBuilder("movieExportsSeedStep", jobRepository)
                .<TmdbExportItem, TmdbWorkQueue>chunk(5000, transactionManager)
                .reader(movieExportItemReader)
                .processor(exportItemProcessor("movie"))
                .writer(workQueueItemWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    @StepScope
    public Step tvExportsSeedStep(JsonItemReader<TmdbExportItem> tvExportItemReader) {
        // ... movieExportsSeedStep과 동일한 구조로 tv용 Step 구현 ...
        return null; // TODO: 구현
    }

    @Bean
    @StepScope
    public Step personExportsSeedStep(JsonItemReader<TmdbExportItem> personExportItemReader) {
        // ... movieExportsSeedStep과 동일한 구조로 person용 Step 구현 ...
        return null; // TODO: 구현
    }

    @Bean
    @StepScope
    public JsonItemReader<TmdbExportItem> movieExportItemReader(@Value("#{jobParameters['fileDate']}") String fileDate) {
        return createExportItemReader("movie", fileDate);
    }

    @Bean
    @StepScope
    public JsonItemReader<TmdbExportItem> tvExportItemReader(@Value("#{jobParameters['fileDate']}") String fileDate) {
        return createExportItemReader("tv", fileDate);
    }

    @Bean
    @StepScope
    public JsonItemReader<TmdbExportItem> personExportItemReader(@Value("#{jobParameters['fileDate']}") String fileDate) {
        return createExportItemReader("person", fileDate);
    }

    private JsonItemReader<TmdbExportItem> createExportItemReader(String entityType, String fileDate) {
        if (fileDate == null) {
            fileDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM_dd_yyyy"));
        }
        String url = String.format(EXPORT_URL_TEMPLATE, entityType, fileDate);
        log.info("Creating ItemReader for TMDB export file: {}", url);

        try {
            return new JsonItemReaderBuilder<TmdbExportItem>()
                    .name(entityType + "ExportReader")
                    .resource(new UrlResource(url))
                    .jsonObjectReader(new JacksonJsonObjectReader<>(TmdbExportItem.class))
                    .build();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL for TMDB export file: " + url, e);
        }
    }

    private ItemProcessor<TmdbExportItem, TmdbWorkQueue> exportItemProcessor(String entityType) {
        return item -> {
            if (item.adult()) {
                return null;
            }
            return TmdbWorkQueue.builder()
                    .entityType(entityType)
                    .tmdbId(item.id())
                    .priority(calculatePriority(item.popularity()))
                    .build();
        };
    }

    private int calculatePriority(double popularity) {
        if (popularity > 100) return 10;
        if (popularity > 50) return 5;
        return 0;
    }

    @Bean
    public JpaItemWriter<TmdbWorkQueue> workQueueItemWriter() {
        JpaItemWriter<TmdbWorkQueue> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}