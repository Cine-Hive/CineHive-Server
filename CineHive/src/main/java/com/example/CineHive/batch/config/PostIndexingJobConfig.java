package com.example.CineHive.batch.config;

import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.post.repository.PostRepository;
import com.example.CineHive.domain.search.document.PostDocument;
import com.example.CineHive.domain.search.repository.PostDocumentRepository;
import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PostIndexingJobConfig {

    private final PostRepository postRepository;
    private final PostDocumentRepository postDocumentRepository;
    private final EntityManagerFactory entityManagerFactory; // JpaPagingItemReader에 필요
    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job postIndexingJob(JobRepository jobRepository, Step postIndexingStep) {
        return new JobBuilder("postIndexingJob", jobRepository)
                .start(postIndexingStep)
                .build();
    }

    @Bean
    public Step postIndexingStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                 ItemReader<Post> postItemReader,
                                 ItemProcessor<Post, PostDocument> postItemProcessor,
                                 ItemWriter<PostDocument> elasticsearchPostItemWriter) {
        return new StepBuilder("postIndexingStep", jobRepository)
                .<Post, PostDocument>chunk(CHUNK_SIZE, tm)
                .reader(postItemReader)
                .processor(postItemProcessor)
                .writer(elasticsearchPostItemWriter)
                .build();
    }

    /**
     * [Reader] DB의 모든 Post 데이터를 페이징하여 읽어옵니다.
     */
    @Bean
    public JpaPagingItemReader<Post> postItemReader() {
        return new JpaPagingItemReaderBuilder<Post>()
                .name("postItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT p FROM Post p ORDER BY p.id ASC")
                .build();
    }

    /**
     * [Processor] Post 엔티티를 PostDocument로 변환합니다.
     */
    @Bean
    public ItemProcessor<Post, PostDocument> postItemProcessor() {
        return PostDocument::from;
    }

    /**
     * [Writer] 변환된 PostDocument를 Elasticsearch에 대량 저장합니다.
     */
    @Bean
    public ItemWriter<PostDocument> elasticsearchPostItemWriter() {
        return items -> postDocumentRepository.saveAll(items);
    }
}