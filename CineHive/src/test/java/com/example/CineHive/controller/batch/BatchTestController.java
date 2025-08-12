package com.example.CineHive.controller.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 배치 작업 수동 실행을 위한 테스트 컨트롤러
 * 개발/테스트 환경에서만 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchTestController {

    private final JobLauncher jobLauncher;
    private final Job tmdbMediaIndexingJob;
    private final Job personIndexingJob;
    private final Job postIndexingJob;
    private final Job userIndexingJob;
    private final Job collectionIndexingJob;

    @PostMapping("/media")
    public String runMediaIndexing() {
        try {
            jobLauncher.run(tmdbMediaIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters());
            return "미디어 인덱싱 작업 시작됨";
        } catch (Exception e) {
            log.error("미디어 인덱싱 작업 실행 중 오류", e);
            return "오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/person")
    public String runPersonIndexing() {
        try {
            jobLauncher.run(personIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters());
            return "인물 인덱싱 작업 시작됨";
        } catch (Exception e) {
            log.error("인물 인덱싱 작업 실행 중 오류", e);
            return "오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/post")
    public String runPostIndexing() {
        try {
            jobLauncher.run(postIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters());
            return "게시글 인덱싱 작업 시작됨";
        } catch (Exception e) {
            log.error("게시글 인덱싱 작업 실행 중 오류", e);
            return "오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/user")
    public String runUserIndexing() {
        try {
            jobLauncher.run(userIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters());
            return "사용자 인덱싱 작업 시작됨";
        } catch (Exception e) {
            log.error("사용자 인덱싱 작업 실행 중 오류", e);
            return "오류 발생: " + e.getMessage();
        }
    }

    @PostMapping("/collection")
    public String runCollectionIndexing() {
        try {
            jobLauncher.run(collectionIndexingJob,
                    new JobParametersBuilder()
                            .addString("datetime", LocalDateTime.now().toString())
                            .toJobParameters());
            return "컬렉션 인덱싱 작업 시작됨";
        } catch (Exception e) {
            log.error("컬렉션 인덱싱 작업 실행 중 오류", e);
            return "오류 발생: " + e.getMessage();
        }
    }
}