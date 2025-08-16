package com.example.CineHive.datasync.controller;

import com.example.CineHive.global.dto.ApiResponse;
import com.example.CineHive.global.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 배치 작업 실행 이력 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/batch/history")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Batch History", description = "배치 작업 실행 이력 API")
public class BatchHistoryController {
    
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    
    /**
     * 작업 실행 이력 조회
     */
    @GetMapping
    @Operation(summary = "배치 실행 이력 조회", description = "지정된 기간의 배치 작업 실행 이력을 조회합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutionHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "fullSyncJob") String jobName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            // 기본값 설정
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(7);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(jobName, page * size, size);
            List<Map<String, Object>> executions = new ArrayList<>();
            
            Date startDateTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDateTime = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            for (JobInstance instance : instances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(instance);
                for (JobExecution execution : jobExecutions) {
                    if (execution.getStartTime() != null) {
                        Date execStartTime = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
                        if (execStartTime.after(startDateTime) && execStartTime.before(endDateTime)) {
                            executions.add(mapJobExecution(execution));
                        }
                    }
                }
            }
            
            Map<String, Object> response = Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "totalCount", executions.size(),
                "page", page,
                "size", size,
                "executions", executions
            );
            
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            log.error("배치 실행 이력 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("배치 실행 이력 조회 중 오류가 발생했습니다")));
        }
    }
    
    /**
     * 작업별 통계 조회
     */
    @GetMapping("/statistics")
    @Operation(summary = "배치 작업 통계 조회", description = "지정된 기간의 배치 작업 통계를 조회합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "fullSyncJob") String jobName) {
        
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusMonths(1);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Date startDateTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDateTime = Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            List<JobInstance> allInstances = jobExplorer.findJobInstancesByJobName(jobName, 0, 1000);
            
            int totalExecutions = 0;
            int successfulExecutions = 0;
            int failedExecutions = 0;
            int stoppedExecutions = 0;
            long totalDuration = 0;
            long totalItemsProcessed = 0;
            long totalItemsFailed = 0;
            Map<String, Integer> dailyExecutions = new TreeMap<>();
            Map<String, Long> stepStatistics = new HashMap<>();
            
            for (JobInstance instance : allInstances) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                for (JobExecution execution : executions) {
                    if (execution.getStartTime() != null) {
                        Date execStartTime = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
                        if (execStartTime.after(startDateTime) && execStartTime.before(endDateTime)) {
                            
                            totalExecutions++;
                        
                        // 상태별 카운트
                        switch (execution.getStatus()) {
                            case COMPLETED -> successfulExecutions++;
                            case FAILED -> failedExecutions++;
                            case STOPPED -> stoppedExecutions++;
                        }
                        
                        // 실행 시간 계산
                        if (execution.getEndTime() != null) {
                            Date execEndTime = Date.from(execution.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
                            Date execStartTimeForDuration = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
                            totalDuration += execEndTime.getTime() - execStartTimeForDuration.getTime();
                        }
                        
                        // 일별 실행 카운트
                        String dateKey = execution.getStartTime().toLocalDate().toString();
                        dailyExecutions.merge(dateKey, 1, Integer::sum);
                        
                        // 스텝별 통계
                        for (StepExecution step : execution.getStepExecutions()) {
                            totalItemsProcessed += step.getWriteCount();
                            totalItemsFailed += step.getSkipCount();
                            
                            String stepKey = step.getStepName();
                            stepStatistics.merge(stepKey + "_read", (long) step.getReadCount(), Long::sum);
                            stepStatistics.merge(stepKey + "_write", (long) step.getWriteCount(), Long::sum);
                            stepStatistics.merge(stepKey + "_skip", (long) step.getSkipCount(), Long::sum);
                        }
                        }
                    }
                }
            }
            
            double successRate = totalExecutions > 0 ? 
                (double) successfulExecutions / totalExecutions * 100 : 0;
            long averageDuration = totalExecutions > 0 ? 
                totalDuration / totalExecutions : 0;
            
            Map<String, Object> statistics = Map.of(
                "period", Map.of("startDate", startDate, "endDate", endDate),
                "summary", Map.of(
                    "totalExecutions", totalExecutions,
                    "successfulExecutions", successfulExecutions,
                    "failedExecutions", failedExecutions,
                    "stoppedExecutions", stoppedExecutions,
                    "successRate", String.format("%.2f%%", successRate),
                    "averageDuration", formatDuration(averageDuration),
                    "totalItemsProcessed", totalItemsProcessed,
                    "totalItemsFailed", totalItemsFailed
                ),
                "dailyExecutions", dailyExecutions,
                "stepStatistics", stepStatistics
            );
            
            return ResponseEntity.ok(ApiResponse.ok(statistics));
        } catch (Exception e) {
            log.error("배치 작업 통계 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("배치 작업 통계 조회 중 오류가 발생했습니다")));
        }
    }
    
    /**
     * 실패한 작업 재실행 이력 조회
     */
    @GetMapping("/retries")
    @Operation(summary = "재시도 이력 조회", description = "실패 후 재시도된 작업들의 이력을 조회합니다")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRetryHistory(
            @RequestParam(defaultValue = "fullSyncJob") String jobName,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(jobName, 0, limit * 5);
            List<Map<String, Object>> retryHistory = new ArrayList<>();
            
            for (JobInstance instance : instances) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
                if (executions.size() > 1) {
                    // 재시도가 있었던 인스턴스
                    Map<String, Object> retryInfo = new HashMap<>();
                    retryInfo.put("instanceId", instance.getInstanceId());
                    retryInfo.put("jobName", instance.getJobName());
                    retryInfo.put("totalAttempts", executions.size());
                    
                    List<Map<String, Object>> attempts = executions.stream()
                        .map(this::mapJobExecutionSummary)
                        .collect(Collectors.toList());
                    retryInfo.put("attempts", attempts);
                    
                    // 최종 상태
                    JobExecution lastExecution = executions.get(0);
                    retryInfo.put("finalStatus", lastExecution.getStatus().toString());
                    retryInfo.put("finalExitCode", lastExecution.getExitStatus().getExitCode());
                    
                    retryHistory.add(retryInfo);
                    
                    if (retryHistory.size() >= limit) {
                        break;
                    }
                }
            }
            
            return ResponseEntity.ok(ApiResponse.ok(retryHistory));
        } catch (Exception e) {
            log.error("재시도 이력 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("재시도 이력 조회 중 오류가 발생했습니다")));
        }
    }
    
    private Map<String, Object> mapJobExecution(JobExecution execution) {
        Map<String, Object> map = new HashMap<>();
        map.put("executionId", execution.getId());
        map.put("jobName", execution.getJobInstance().getJobName());
        map.put("status", execution.getStatus().toString());
        map.put("startTime", execution.getStartTime());
        map.put("endTime", execution.getEndTime());
        map.put("duration", calculateDuration(execution));
        map.put("exitCode", execution.getExitStatus().getExitCode());
        map.put("exitDescription", execution.getExitStatus().getExitDescription());
        
        // 스텝 요약
        List<String> stepSummary = execution.getStepExecutions().stream()
            .map(step -> String.format("%s: %s (R:%d/W:%d/S:%d)",
                step.getStepName(),
                step.getStatus(),
                step.getReadCount(),
                step.getWriteCount(),
                step.getSkipCount()))
            .collect(Collectors.toList());
        map.put("stepSummary", stepSummary);
        
        return map;
    }
    
    private Map<String, Object> mapJobExecutionSummary(JobExecution execution) {
        Map<String, Object> map = new HashMap<>();
        map.put("executionId", execution.getId());
        map.put("status", execution.getStatus().toString());
        map.put("startTime", execution.getStartTime());
        map.put("endTime", execution.getEndTime());
        map.put("exitCode", execution.getExitStatus().getExitCode());
        return map;
    }
    
    private String calculateDuration(JobExecution execution) {
        if (execution.getStartTime() == null) {
            return "N/A";
        }
        
        if (execution.getEndTime() != null) {
            Date endTime = Date.from(execution.getEndTime().atZone(ZoneId.systemDefault()).toInstant());
            Date startTime = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            long duration = endTime.getTime() - startTime.getTime();
            return formatDuration(duration);
        } else if (execution.getStartTime() != null) {
            Date startTime = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
            long duration = System.currentTimeMillis() - startTime.getTime();
            return formatDuration(duration);
        }
        return "N/A";
    }
    
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}