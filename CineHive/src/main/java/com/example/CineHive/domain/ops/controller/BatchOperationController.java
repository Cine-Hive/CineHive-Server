package com.example.CineHive.domain.ops.controller;

import com.example.CineHive.domain.ops.service.BatchOperationService;
import com.example.CineHive.domain.ops.dto.BatchExecutionResponse;
import com.example.CineHive.domain.ops.dto.BatchStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/ops/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Operations", description = "배치 작업 운영 API (관리자 전용)")
@PreAuthorize("hasRole('ADMIN')")
public class BatchOperationController {

    private final BatchOperationService batchOperationService;
    
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM_dd_yyyy");

    @PostMapping("/start-full-sync")
    @Operation(
        summary = "Full Sync 배치 작업 시작",
        description = "TMDB Daily Export를 이용한 전체 동기화 배치를 실행합니다. " +
                     "Export Seeding → Detail Processing 순으로 진행됩니다."
    )
    public ResponseEntity<BatchExecutionResponse> startFullSync(
            @Parameter(description = "파일 날짜 (MM_dd_yyyy 형식), 기본값: 어제", example = "12_25_2023")
            @RequestParam(required = false) String fileDate
    ) {
        try {
            // 파일 날짜가 없으면 어제로 설정
            if (fileDate == null || fileDate.isBlank()) {
                fileDate = LocalDate.now().minusDays(1).format(FILE_DATE_FORMATTER);
            }
            
            log.info("Full Sync 배치 작업 시작 요청: fileDate={}", fileDate);
            
            BatchExecutionResponse response = batchOperationService.startFullSyncJob(fileDate);
            
            log.info("Full Sync 배치 작업 시작 완료: jobExecutionId={}", response.getJobExecutionId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Full Sync 배치 작업 시작 실패: fileDate={}", fileDate, e);
            return ResponseEntity.internalServerError()
                    .body(BatchExecutionResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status/{jobExecutionId}")
    @Operation(
        summary = "배치 작업 상태 조회",
        description = "특정 Job Execution의 현재 상태와 진행률을 조회합니다."
    )
    public ResponseEntity<BatchStatusResponse> getBatchStatus(
            @Parameter(description = "Job Execution ID")
            @PathVariable Long jobExecutionId
    ) {
        try {
            BatchStatusResponse response = batchOperationService.getBatchStatus(jobExecutionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("배치 상태 조회 실패: jobExecutionId={}", jobExecutionId, e);
            return ResponseEntity.internalServerError()
                    .body(BatchStatusResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/queue-stats")
    @Operation(
        summary = "Work Queue 통계 조회",
        description = "TMDB Work Queue의 처리 현황 통계를 조회합니다."
    )
    public ResponseEntity<?> getQueueStats() {
        try {
            var stats = batchOperationService.getQueueStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("큐 통계 조회 실패", e);
            return ResponseEntity.internalServerError().body("Queue stats retrieval failed: " + e.getMessage());
        }
    }

    @PostMapping("/clear-failed-queue")
    @Operation(
        summary = "실패한 큐 아이템 정리",
        description = "처리 실패한 Work Queue 아이템들을 정리합니다."
    )
    public ResponseEntity<String> clearFailedQueueItems() {
        try {
            int clearedCount = batchOperationService.clearFailedQueueItems();
            return ResponseEntity.ok("실패한 큐 아이템 " + clearedCount + "개를 정리했습니다.");
        } catch (Exception e) {
            log.error("실패 큐 아이템 정리 실패", e);
            return ResponseEntity.internalServerError().body("Failed queue cleanup failed: " + e.getMessage());
        }
    }
}