package com.example.CineHive.batch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Batch Controller", description = "데이터 색인 등 배치 작업 수동 실행 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;

    @Qualifier("tmdbMediaIndexingJob")
    private final Job tmdbMediaIndexingJob;

    @Qualifier("postIndexingJob")
    private final Job postIndexingJob;

    @Qualifier("userIndexingJob")
    private final Job userIndexingJob;

    @Qualifier("personIndexingJob")
    private final Job personIndexingJob;

    @Qualifier("collectionIndexingJob")
    private final Job collectionIndexingJob;

    @Operation(summary = "TMDB 미디어/컬렉션 색인 Job 실행", description = "TMDB API에서 변경된 영화/TV/컬렉션 정보를 가져와 DB와 Elasticsearch에 색인합니다.")
    @PostMapping("/jobs/media-indexing")
    public ResponseEntity<String> launchMediaIndexingJob() {
        return runJob(tmdbMediaIndexingJob, "mediaIndexing");
    }

    @Operation(summary = "게시글 색인 Job 실행", description = "DB의 모든 게시글 정보를 Elasticsearch에 색인합니다.")
    @PostMapping("/jobs/post-indexing")
    public ResponseEntity<String> launchPostIndexingJob() {
        return runJob(postIndexingJob, "postIndexing");
    }

    @Operation(summary = "사용자 색인 Job 실행", description = "DB의 모든 사용자 정보를 Elasticsearch에 색인합니다.")
    @PostMapping("/jobs/user-indexing")
    public ResponseEntity<String> launchUserIndexingJob() {
        return runJob(userIndexingJob, "userIndexing");
    }

    @Operation(summary = "인물 색인 Job 실행", description = "DB의 모든 인물 정보를 Elasticsearch에 색인합니다.")
    @PostMapping("/jobs/person-indexing")
    public ResponseEntity<String> launchPersonIndexingJob() {
        return runJob(personIndexingJob, "personIndexing");
    }

    @Operation(summary = "컬렉션 색인 Job 실행", description = "DB의 모든 컬렉션 정보를 Elasticsearch에 색인합니다.")
    @PostMapping("/jobs/collection-indexing")
    public ResponseEntity<String> launchCollectionIndexingJob() {
        return runJob(collectionIndexingJob, "collectionIndexing");
    }

    /**
     * Job을 실행하고 응답을 반환하는 공통 메서드
     * @param job 실행할 Job 객체
     * @param jobName 로그에 표시될 Job 이름
     * @return 실행 결과 응답
     */
    private ResponseEntity<String> runJob(Job job, String jobName) {
        try {
            log.info("{} Job 실행 요청.", jobName);
            JobParameters params = new JobParametersBuilder()
                    .addString("datetime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(job, params);
            return ResponseEntity.accepted().body(jobName + " Job이 성공적으로 시작되었습니다.");
        } catch (Exception e) {
            log.error("{} Job 실행 중 오류 발생", jobName, e);
            return ResponseEntity.internalServerError().body(jobName + " Job 실행에 실패했습니다: " + e.getMessage());
        }
    }
}