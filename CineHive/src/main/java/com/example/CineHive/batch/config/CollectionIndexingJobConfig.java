package com.example.CineHive.batch.config;

import com.example.CineHive.domain.collection.entity.Collection;
import com.example.CineHive.domain.search.document.CollectionDocument;
import com.example.CineHive.domain.search.repository.CollectionDocumentRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CollectionIndexingJobConfig {

    private final CollectionDocumentRepository collectionDocumentRepository;
    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job collectionIndexingJob(JobRepository jobRepository, Step collectionIndexingStep) {
        return new JobBuilder("collectionIndexingJob", jobRepository)
                .start(collectionIndexingStep)
                .build();
    }

    @Bean
    public Step collectionIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("collectionIndexingStep", jobRepository)
                .<Collection, CollectionDocument>chunk(CHUNK_SIZE, tm)
                .reader(collectionItemReader())
                .processor(collectionItemProcessor())
                .writer(elasticsearchCollectionItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Collection> collectionItemReader() {
        return new JpaPagingItemReaderBuilder<Collection>()
                .name("collectionItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT c FROM Collection c ORDER BY c.id ASC")
                .build();
    }

    @Bean
    public ItemProcessor<Collection, CollectionDocument> collectionItemProcessor() {
        return CollectionDocument::from;
    }

    @Bean
    public ItemWriter<CollectionDocument> elasticsearchCollectionItemWriter() {
        return items -> collectionDocumentRepository.saveAll(items);
    }
}