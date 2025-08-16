package com.example.CineHive.datasync.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchJobRunner {
    
    private final JobLauncher jobLauncher;
    private final Job fullSyncJob;
    
    @Bean
    @Profile("local")
    @ConditionalOnProperty(name = "RUN_BATCH_JOB", havingValue = "true", matchIfMissing = false)
    public CommandLineRunner runBatchJob() {
        return args -> {
            try {
                log.info("=== STARTING BATCH JOB MANUALLY ===");
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();
                
                jobLauncher.run(fullSyncJob, jobParameters);
                log.info("=== BATCH JOB STARTED ===");
            } catch (Exception e) {
                log.error("Failed to start batch job", e);
            }
        };
    }
}