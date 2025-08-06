package com.example.CineHive.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaIndexingScheduler {

    private final JobLauncher jobLauncher;
    private final Job tmdbMediaIndexingJob;

    // 매일 정오와 자정에 실행 (cron = "0 0 0,12 * * *")
    @Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
    public void runTmdbMediaIndexingJob() {
        try {
            log.info("TMDB 미디어 색인 배치 작업을 시작합니다.");
            jobLauncher.run(
                    tmdbMediaIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters()
            );
        } catch (Exception e) {
            log.error("TMDB 미디어 색인 배치 작업 실행 중 오류 발생", e);
        }
    }
}