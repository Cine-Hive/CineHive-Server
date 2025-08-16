package com.example.CineHive.datasync.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 배치 작업 대시보드 응답 DTO
 * 현재 실행 중인 작업과 전체 통계를 포함
 */
public record BatchDashboardResponse(
    // 현재 작업 상태
    CurrentJobStatus currentJob,
    
    // 큐 상태
    QueueStatus queueStatus,
    
    // 오늘의 통계
    DailyStatistics todayStats,
    
    // 최근 실행 이력
    RecentExecutions recentExecutions,
    
    // 시스템 상태
    SystemHealth systemHealth
) {
    
    public record CurrentJobStatus(
        String jobName,
        String status,
        LocalDateTime startTime,
        Long duration,
        String currentStep,
        Double progress,
        Map<String, StepProgress> stepProgress
    ) {}
    
    public record StepProgress(
        String stepName,
        String status,
        Integer readCount,
        Integer writeCount,
        Integer skipCount,
        LocalDateTime startTime,
        Long duration
    ) {}
    
    public record QueueStatus(
        Long totalItems,
        Long pendingItems,
        Long processingItems,
        Long processedItems,
        Long failedItems,
        Map<String, Long> itemsByType
    ) {}
    
    public record DailyStatistics(
        Integer totalExecutions,
        Integer successfulExecutions,
        Integer failedExecutions,
        Long totalItemsProcessed,
        Long totalItemsFailed,
        Double averageProcessingTime,
        Map<String, Long> processedByType
    ) {}
    
    public record RecentExecutions(
        java.util.List<JobExecutionSummary> executions
    ) {}
    
    public record JobExecutionSummary(
        Long executionId,
        String jobName,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long duration,
        Integer totalSteps,
        Integer completedSteps,
        String exitCode,
        String exitMessage
    ) {}
    
    public record SystemHealth(
        Double cpuUsage,
        Double memoryUsage,
        Long activeThreads,
        Integer apiRateLimitRemaining,
        LocalDateTime nextScheduledRun,
        Boolean isHealthy
    ) {}
}