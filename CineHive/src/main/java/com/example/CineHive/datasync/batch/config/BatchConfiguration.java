package com.example.CineHive.datasync.batch.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Batch 추가 설정
 * JobExplorer, JobOperator 등 배치 관리 기능 제공
 */
@Configuration
@EnableScheduling
public class BatchConfiguration {

    /**
     * JobOperator Bean 생성
     * 배치 작업 제어를 위한 고수준 인터페이스 제공
     */
    @Bean
    public JobOperator jobOperator(JobLauncher jobLauncher,
                                  JobRepository jobRepository,
                                  JobRegistry jobRegistry,
                                  JobExplorer jobExplorer) {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobLauncher(jobLauncher);
        jobOperator.setJobRepository(jobRepository);
        jobOperator.setJobRegistry(jobRegistry);
        jobOperator.setJobExplorer(jobExplorer);
        return jobOperator;
    }
    
    /**
     * JobRegistry Post Processor
     * Job들을 자동으로 JobRegistry에 등록
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }
}