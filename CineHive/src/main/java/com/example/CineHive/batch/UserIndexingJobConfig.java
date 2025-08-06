package com.example.CineHive.batch;

import com.example.CineHive.domain.search.document.UserDocument;
import com.example.CineHive.domain.search.repository.UserDocumentRepository;
import com.example.CineHive.domain.user.entity.User;
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
public class UserIndexingJobConfig {

    private final UserDocumentRepository userDocumentRepository;
    private final EntityManagerFactory entityManagerFactory;
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job userIndexingJob(JobRepository jobRepository, Step userIndexingStep) {
        return new JobBuilder("userIndexingJob", jobRepository)
                .start(userIndexingStep)
                .build();
    }

    @Bean
    public Step userIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("userIndexingStep", jobRepository)
                .<User, UserDocument>chunk(CHUNK_SIZE, tm)
                .reader(userItemReader())
                .processor(userItemProcessor())
                .writer(elasticsearchUserItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<User> userItemReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT u FROM User u ORDER BY u.id ASC")
                .build();
    }

    @Bean
    public ItemProcessor<User, UserDocument> userItemProcessor() {
        return UserDocument::from;
    }

    @Bean
    public ItemWriter<UserDocument> elasticsearchUserItemWriter() {
        return items -> userDocumentRepository.saveAll(items);
    }
}