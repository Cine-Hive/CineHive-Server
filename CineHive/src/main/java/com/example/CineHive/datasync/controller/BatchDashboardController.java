package com.example.CineHive.datasync.controller;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.service.BatchMonitoringService;
import com.example.CineHive.datasync.dto.BatchDashboardResponse;
import com.example.CineHive.global.dto.ApiResponse;
import com.example.CineHive.global.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 배치 작업 대시보드 API 컨트롤러
 * 실시간 모니터링과 통계 정보 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/batch/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Batch Dashboard", description = "배치 작업 대시보드 API")
public class BatchDashboardController {
    
    private final BatchMonitoringService monitoringService;
    
    /**
     * 대시보드 전체 데이터 조회
     */
    @GetMapping
    @Operation(summary = "배치 대시보드 조회", description = "실시간 배치 작업 상태와 통계를 조회합니다")
    public ResponseEntity<ApiResponse<BatchDashboardResponse>> getDashboard() {
        try {
            BatchDashboardResponse dashboard = monitoringService.getDashboardData();
            return ResponseEntity.ok(ApiResponse.ok(dashboard));
        } catch (Exception e) {
            log.error("대시보드 데이터 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("대시보드 데이터 조회 중 오류가 발생했습니다")));
        }
    }
    
    /**
     * 특정 작업 실행의 상세 정보 조회
     */
    @GetMapping("/executions/{executionId}")
    @Operation(summary = "작업 실행 상세 조회", description = "특정 배치 작업 실행의 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutionDetail(
            @PathVariable Long executionId) {
        try {
            JobExecution execution = monitoringService.getJobExecutionDetail(executionId);
            if (execution == null) {
                return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("실행 정보를 찾을 수 없습니다")));
            }
            
            Map<String, Object> detail = Map.of(
                "jobName", execution.getJobInstance().getJobName(),
                "executionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "startTime", execution.getStartTime(),
                "endTime", execution.getEndTime(),
                "exitStatus", execution.getExitStatus().getExitCode(),
                "exitDescription", execution.getExitStatus().getExitDescription(),
                "jobParameters", execution.getJobParameters().getParameters(),
                "steps", execution.getStepExecutions().stream()
                    .map(this::mapStepExecution)
                    .collect(Collectors.toList())
            );
            
            return ResponseEntity.ok(ApiResponse.ok(detail));
        } catch (Exception e) {
            log.error("작업 실행 상세 조회 실패: executionId={}", executionId, e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("작업 실행 정보 조회 중 오류가 발생했습니다")));
        }
    }
    
    /**
     * 실패한 아이템 목록 조회
     */
    @GetMapping("/failed-items")
    @Operation(summary = "실패 아이템 조회", description = "처리 실패한 작업 큐 아이템을 조회합니다")
    public ResponseEntity<ApiResponse<List<TmdbWorkQueue>>> getFailedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<TmdbWorkQueue> failedItems = monitoringService.getFailedItems(page, size);
            return ResponseEntity.ok(ApiResponse.ok(failedItems));
        } catch (Exception e) {
            log.error("실패 아이템 조회 실패", e);
            return ResponseEntity.ok(ApiResponse.error(ErrorResponse.of("실패 아이템 조회 중 오류가 발생했습니다")));
        }
    }
    
    /**
     * 실시간 진행 상황 조회 (SSE 스트리밍)
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    @Operation(summary = "실시간 진행 상황 스트림", description = "Server-Sent Events를 통한 실시간 진행 상황 스트리밍")
    public ResponseEntity<String> streamProgress() {
        // SSE 구현은 별도의 SseEmitter를 사용해야 하므로 간단한 예시만 제공
        return ResponseEntity.ok()
            .header("Content-Type", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .header("Connection", "keep-alive")
            .body("data: {\"message\": \"SSE endpoint ready\"}\n\n");
    }
    
    private Map<String, Object> mapStepExecution(StepExecution step) {
        return Map.of(
            "stepName", step.getStepName(),
            "status", step.getStatus().toString(),
            "readCount", step.getReadCount(),
            "writeCount", step.getWriteCount(),
            "skipCount", step.getSkipCount(),
            "commitCount", step.getCommitCount(),
            "rollbackCount", step.getRollbackCount(),
            "startTime", step.getStartTime() != null ? step.getStartTime().toString() : "",
            "endTime", step.getEndTime() != null ? step.getEndTime().toString() : "",
            "exitStatus", step.getExitStatus().getExitCode()
        );
    }
}