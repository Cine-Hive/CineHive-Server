package com.example.CineHive.datasync.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 배치 테스트용 컨트롤러 (local 프로파일에서만 활성화)
 */
@Slf4j
@RestController
@RequestMapping("/api/test/batch")
@RequiredArgsConstructor
@Profile("local")
public class BatchTestController {
    
    private final JobLauncher jobLauncher;
    private final Job fullSyncJob;
    
    /**
     * 배치 작업 수동 실행 (인증 없이)
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startBatch() {
        log.info("=== 배치 수동 실행 요청 ===");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            var execution = jobLauncher.run(fullSyncJob, jobParameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "배치 작업이 시작되었습니다.");
            response.put("executionId", execution.getId());
            response.put("status", execution.getStatus().toString());
            
            log.info("배치 작업 시작: executionId={}, status={}", 
                execution.getId(), execution.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 작업 시작 실패", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "배치 작업 시작 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}