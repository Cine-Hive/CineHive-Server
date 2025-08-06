package com.example.CineHive.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostIndexingScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("postIndexingJob")
    private final Job postIndexingJob;

    // 매 시간마다 실행 (cron = "0 0 * * * *")
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void runPostIndexingJob() {
        try {
            log.info("게시글 데이터 색인 배치 작업을 시작합니다.");
            jobLauncher.run(
                    postIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters()
            );
        } catch (Exception e) {
            log.error("게시글 데이터 색인 배치 작업 실행 중 오류 발생", e);
        }
    }
}