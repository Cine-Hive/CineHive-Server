package com.example.CineHive.batch.config;

import com.example.CineHive.batch.common.PaginatedTmdbItemReader;
import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbPersonInListResponse;
import com.example.CineHive.domain.search.document.PersonDocument;
import com.example.CineHive.domain.search.repository.PersonDocumentRepository;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PersonIndexingJobConfig {
    private final TmdbApiClient tmdbApiClient;
    private final PersonDocumentRepository personDocumentRepository;
    private static final int CHUNK_SIZE = 200;

    @Bean
    public Job personIndexingJob(JobRepository jobRepository, Step personIndexingStep) {
        return new JobBuilder("personIndexingJob", jobRepository)
                .start(personIndexingStep)
                .build();
    }

    @Bean
    public Step personIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                   ItemReader<TmdbPersonInListResponse> paginatedPersonItemReader,
                                   ItemProcessor<TmdbPersonInListResponse, PersonDocument> personItemProcessor,
                                   ItemWriter<PersonDocument> elasticsearchPersonItemWriter) {
        return new StepBuilder("personIndexingStep", jobRepository)
                .<TmdbPersonInListResponse, PersonDocument>chunk(CHUNK_SIZE, tm)
                .reader(paginatedPersonItemReader)
                .processor(personItemProcessor)
                .writer(elasticsearchPersonItemWriter)
                .build();
    }

    @Bean
    public ItemReader<TmdbPersonInListResponse> paginatedPersonItemReader() {
        return new PaginatedTmdbItemReader<>(tmdbApiClient::getPopularPeople);
    }

    @Bean
    public ItemProcessor<TmdbPersonInListResponse, PersonDocument> personItemProcessor() {
        return PersonDocument::from;
    }

    @Bean
    public ItemWriter<PersonDocument> elasticsearchPersonItemWriter() {
        return items -> {
            if (!items.isEmpty()) personDocumentRepository.saveAll(items);
        };
    }
}