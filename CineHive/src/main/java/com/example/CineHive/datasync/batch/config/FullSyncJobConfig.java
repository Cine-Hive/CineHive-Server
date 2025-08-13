package com.example.CineHive.datasync.batch.config;

import com.example.CineHive.datasync.batch.tasklet.ExportDownloadTasklet;
import com.example.CineHive.datasync.batch.reader.TmdbWorkQueueReader;
import com.example.CineHive.datasync.batch.writer.TmdbExportWriter;
import com.example.CineHive.datasync.batch.writer.MovieSyncWriter;
import com.example.CineHive.datasync.dto.TmdbExportItem;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.JsonLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FullSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    // Tasklets
    private final ExportDownloadTasklet exportDownloadTasklet;
    
    // Readers
    private final TmdbWorkQueueReader tmdbWorkQueueReader;
    
    // Writers
    private final TmdbExportWriter tmdbExportWriter;
    private final MovieSyncWriter movieSyncWriter;

    private static final int EXPORT_CHUNK_SIZE = 5000;
    private static final int DETAIL_CHUNK_SIZE = 100;

    @Bean
    public Job fullSyncJob() {
        return new JobBuilder("fullSyncJob", jobRepository)
                .start(exportDownloadStep())
                .next(exportSeedingStep())
                .next(detailProcessingStep())
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
                .<TmdbExportItem, TmdbExportItem>chunk(EXPORT_CHUNK_SIZE, transactionManager)
                .reader(tmdbExportItemReader())
                .writer(tmdbExportWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(1000)
                .build();
    }

    @Bean
    public Step detailProcessingStep() {
        return new StepBuilder("detailProcessingStep", jobRepository)
                .<TmdbWorkQueue, TmdbWorkQueue>chunk(DETAIL_CHUNK_SIZE, transactionManager)
                .reader(tmdbWorkQueueReader)
                .writer(movieSyncWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public FlatFileItemReader<TmdbExportItem> tmdbExportItemReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter("");  // 전체 라인을 하나의 필드로 처리
        tokenizer.setNames("jsonLine");
        
        return new FlatFileItemReaderBuilder<TmdbExportItem>()
                .name("tmdbExportItemReader")
                .resource(new FileSystemResource(new File("temp/movie_export.json")))
                .lineTokenizer(tokenizer)
                .fieldSetMapper(new TmdbExportFieldSetMapper())
                .build();
    }
}