package com.example.CineHive.batch;

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
public class UserIndexingScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("userIndexingJob")
    private final Job userIndexingJob;

    // 매 시간마다 실행
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void runUserIndexingJob() {
        try {
            log.info("사용자 데이터 색인 배치 작업을 시작합니다.");
            jobLauncher.run(
                    userIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters()
            );
        } catch (Exception e) {
            log.error("사용자 데이터 색인 배치 작업 실행 중 오류 발생", e);
        }
    }
}