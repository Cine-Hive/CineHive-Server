package com.example.CineHive.datasync.controller;

import com.example.CineHive.datasync.batch.scheduler.TmdbSyncScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 배치 작업 관리 API 컨트롤러
 * 관리자만 접근 가능
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BatchAdminController {

    @Autowired(required = false)
    private TmdbSyncScheduler tmdbSyncScheduler;
    
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;

    /**
     * 수동으로 배치 작업 실행
     */
    @PostMapping("/sync/start")
    public ResponseEntity<Map<String, Object>> startManualSync(
            @RequestParam(defaultValue = "FULL") String syncType) {
        
        log.info("관리자 수동 동기화 요청: syncType={}", syncType);
        
        try {
            if (tmdbSyncScheduler == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "배치 스케줄러가 활성화되지 않았습니다. (local 프로파일에서는 사용 불가)");
                return ResponseEntity.ok(response);
            }
            
            tmdbSyncScheduler.runManualSync(syncType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "배치 작업이 시작되었습니다.");
            response.put("syncType", syncType);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 작업 시작 실패", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "배치 작업 시작 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 실행 중인 배치 작업 중지
     */
    @PostMapping("/sync/stop/{executionId}")
    public ResponseEntity<Map<String, Object>> stopBatchJob(@PathVariable Long executionId) {
        log.info("배치 작업 중지 요청: executionId={}", executionId);
        
        try {
            jobOperator.stop(executionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "배치 작업 중지 요청이 전송되었습니다.");
            response.put("executionId", executionId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 작업 중지 실패", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "배치 작업 중지 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 배치 작업 실행 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBatchStatus() {
        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions("fullSyncJob");
            
            List<Map<String, Object>> runningJobs = runningExecutions.stream()
                    .map(execution -> {
                        Map<String, Object> jobInfo = new HashMap<>();
                        jobInfo.put("id", execution.getId());
                        jobInfo.put("jobName", execution.getJobInstance().getJobName());
                        jobInfo.put("status", execution.getStatus().toString());
                        jobInfo.put("startTime", execution.getStartTime());
                        jobInfo.put("createTime", execution.getCreateTime());
                        jobInfo.put("parameters", execution.getJobParameters().getParameters());
                        return jobInfo;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("runningJobs", runningJobs);
            response.put("runningCount", runningJobs.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 상태 조회 실패", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "배치 상태 조회 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 실패한 배치 작업 재시작
     */
    @PostMapping("/restart/{executionId}")
    public ResponseEntity<Map<String, Object>> restartBatchJob(@PathVariable Long executionId) {
        log.info("배치 작업 재시작 요청: executionId={}", executionId);
        
        try {
            Long newExecutionId = jobOperator.restart(executionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "배치 작업이 재시작되었습니다.");
            response.put("oldExecutionId", executionId);
            response.put("newExecutionId", newExecutionId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 작업 재시작 실패", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "배치 작업 재시작 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}