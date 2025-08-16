package com.example.CineHive.datasync.domain.service;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import com.example.CineHive.datasync.dto.BatchDashboardResponse;
import com.example.CineHive.datasync.dto.BatchDashboardResponse.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Set;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 배치 작업 모니터링 서비스
 * 실시간 대시보드 데이터와 통계 정보 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchMonitoringService {
    
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;
    private final TmdbWorkQueueRepository workQueueRepository;
    
    /**
     * 배치 대시보드 데이터 조회
     */
    @Transactional(readOnly = true)
    public BatchDashboardResponse getDashboardData() {
        return new BatchDashboardResponse(
            getCurrentJobStatus(),
            getQueueStatus(),
            getTodayStatistics(),
            getRecentExecutions(),
            getSystemHealth()
        );
    }
    
    /**
     * 현재 실행 중인 작업 상태 조회
     */
    private CurrentJobStatus getCurrentJobStatus() {
        Set<JobExecution> runningJobsSet = jobExplorer.findRunningJobExecutions("fullSyncJob");
        List<JobExecution> runningJobs = new ArrayList<>(runningJobsSet);
        
        if (runningJobs.isEmpty()) {
            return null;
        }
        
        JobExecution currentExecution = runningJobs.get(0);
        Map<String, StepProgress> stepProgressMap = new HashMap<>();
        
        for (StepExecution stepExecution : currentExecution.getStepExecutions()) {
            StepProgress stepProgress = new StepProgress(
                stepExecution.getStepName(),
                stepExecution.getStatus().toString(),
                (int) stepExecution.getReadCount(),
                (int) stepExecution.getWriteCount(),
                (int) stepExecution.getSkipCount(),
                stepExecution.getStartTime(),
                calculateDurationFromLocalDateTime(stepExecution.getStartTime(), stepExecution.getEndTime())
            );
            stepProgressMap.put(stepExecution.getStepName(), stepProgress);
        }
        
        // 진행률 계산
        double progress = calculateJobProgress(currentExecution);
        
        return new CurrentJobStatus(
            currentExecution.getJobInstance().getJobName(),
            currentExecution.getStatus().toString(),
            currentExecution.getStartTime(),
            calculateDurationFromLocalDateTime(currentExecution.getStartTime(), currentExecution.getEndTime()),
            getCurrentStepName(currentExecution),
            progress,
            stepProgressMap
        );
    }
    
    /**
     * 작업 큐 상태 조회
     */
    private QueueStatus getQueueStatus() {
        Long totalItems = workQueueRepository.count();
        Long pendingItems = workQueueRepository.countByStatus(TmdbWorkQueue.ProcessStatus.PENDING);
        Long processingItems = workQueueRepository.countByStatus(TmdbWorkQueue.ProcessStatus.PROCESSING);
        Long processedItems = workQueueRepository.countByStatus(TmdbWorkQueue.ProcessStatus.DONE);
        Long failedItems = workQueueRepository.countByStatus(TmdbWorkQueue.ProcessStatus.FAILED);
        
        // 타입별 통계
        Map<String, Long> itemsByType = new HashMap<>();
        itemsByType.put("MOVIE", workQueueRepository.countByEntityType(TmdbWorkQueue.EntityType.MOVIE));
        itemsByType.put("TV", workQueueRepository.countByEntityType(TmdbWorkQueue.EntityType.TV));
        itemsByType.put("PERSON", workQueueRepository.countByEntityType(TmdbWorkQueue.EntityType.PERSON));
        // COLLECTION, SEASON, EPISODE는 아직 EntityType enum에 정의되지 않았을 수 있음
        // 필요시 TmdbWorkQueue.EntityType에 추가 필요
        
        return new QueueStatus(
            totalItems,
            pendingItems,
            processingItems,
            processedItems,
            failedItems,
            itemsByType
        );
    }
    
    /**
     * 오늘의 통계 조회
     */
    private DailyStatistics getTodayStatistics() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
        
        List<JobInstance> todayInstances = jobExplorer.findJobInstancesByJobName("fullSyncJob", 0, 100);
        List<JobExecution> todayExecutions = new ArrayList<>();
        
        for (JobInstance instance : todayInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                if (execution.getStartTime() != null) {
                    Date execStartTime = Date.from(execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant());
                    if (execStartTime.after(startDate)) {
                        todayExecutions.add(execution);
                    }
                }
            }
        }
        
        int totalExecutions = todayExecutions.size();
        int successfulExecutions = (int) todayExecutions.stream()
            .filter(e -> e.getStatus() == BatchStatus.COMPLETED)
            .count();
        int failedExecutions = (int) todayExecutions.stream()
            .filter(e -> e.getStatus() == BatchStatus.FAILED)
            .count();
        
        // 처리된 아이템 수 계산
        long totalItemsProcessed = todayExecutions.stream()
            .flatMap(e -> e.getStepExecutions().stream())
            .mapToLong(StepExecution::getWriteCount)
            .sum();
        
        long totalItemsFailed = todayExecutions.stream()
            .flatMap(e -> e.getStepExecutions().stream())
            .mapToLong(StepExecution::getSkipCount)
            .sum();
        
        // 평균 처리 시간 계산
        double averageProcessingTime = todayExecutions.stream()
            .mapToLong(e -> calculateDurationFromLocalDateTime(e.getStartTime(), e.getEndTime()))
            .average()
            .orElse(0.0);
        
        // 타입별 처리 통계
        Map<String, Long> processedByType = new HashMap<>();
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        processedByType.put("MOVIE", workQueueRepository.countProcessedBetween(
            TmdbWorkQueue.EntityType.MOVIE, startOfDay, endOfDay));
        processedByType.put("TV", workQueueRepository.countProcessedBetween(
            TmdbWorkQueue.EntityType.TV, startOfDay, endOfDay));
        processedByType.put("PERSON", workQueueRepository.countProcessedBetween(
            TmdbWorkQueue.EntityType.PERSON, startOfDay, endOfDay));
        
        return new DailyStatistics(
            totalExecutions,
            successfulExecutions,
            failedExecutions,
            totalItemsProcessed,
            totalItemsFailed,
            averageProcessingTime,
            processedByType
        );
    }
    
    /**
     * 최근 실행 이력 조회
     */
    private RecentExecutions getRecentExecutions() {
        List<JobInstance> recentInstances = jobExplorer.findJobInstancesByJobName("fullSyncJob", 0, 10);
        List<JobExecutionSummary> summaries = new ArrayList<>();
        
        for (JobInstance instance : recentInstances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                JobExecutionSummary summary = new JobExecutionSummary(
                    execution.getId(),
                    execution.getJobInstance().getJobName(),
                    execution.getStatus().toString(),
                    execution.getStartTime(),
                    execution.getEndTime(),
                    calculateDurationFromLocalDateTime(execution.getStartTime(), execution.getEndTime()),
                    execution.getStepExecutions().size(),
                    (int) execution.getStepExecutions().stream()
                        .filter(s -> s.getStatus() == BatchStatus.COMPLETED)
                        .count(),
                    execution.getExitStatus().getExitCode(),
                    execution.getExitStatus().getExitDescription()
                );
                summaries.add(summary);
            }
        }
        
        return new RecentExecutions(summaries);
    }
    
    /**
     * 시스템 헬스 체크
     */
    private SystemHealth getSystemHealth() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        double cpuUsage = osBean.getSystemLoadAverage() > 0 ? osBean.getSystemLoadAverage() * 10 : 5.0;
        double memoryUsage = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory() * 100;
        long activeThreads = Thread.activeCount();
        
        // API 레이트 리밋은 실제 구현 시 TMDB API 클라이언트에서 가져와야 함
        int apiRateLimitRemaining = 40; // 임시 값
        
        // 다음 스케줄 실행 시간 (매일 새벽 2시)
        LocalDateTime nextScheduledRun = LocalDateTime.now()
            .plusDays(1)
            .withHour(2)
            .withMinute(0)
            .withSecond(0);
        if (nextScheduledRun.isBefore(LocalDateTime.now().plusHours(24))) {
            // 오늘 새벽 2시가 아직 안 지났으면
            if (LocalDateTime.now().getHour() < 2) {
                nextScheduledRun = LocalDateTime.now()
                    .withHour(2)
                    .withMinute(0)
                    .withSecond(0);
            }
        }
        
        boolean isHealthy = cpuUsage < 80 && memoryUsage < 90 && apiRateLimitRemaining > 10;
        
        return new SystemHealth(
            cpuUsage,
            memoryUsage,
            activeThreads,
            apiRateLimitRemaining,
            nextScheduledRun,
            isHealthy
        );
    }
    
    // 유틸리티 메서드들
    
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
    
    private Long calculateDurationFromLocalDateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) return 0L;
        if (end == null) {
            return System.currentTimeMillis() - Date.from(start.atZone(ZoneId.systemDefault()).toInstant()).getTime();
        }
        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
        return endDate.getTime() - startDate.getTime();
    }
    
    private Long calculateDuration(Date start, Date end) {
        if (start == null) return 0L;
        if (end == null) {
            return System.currentTimeMillis() - start.getTime();
        }
        return end.getTime() - start.getTime();
    }
    
    private String getCurrentStepName(JobExecution execution) {
        return execution.getStepExecutions().stream()
            .filter(s -> s.getStatus() == BatchStatus.STARTED)
            .map(StepExecution::getStepName)
            .findFirst()
            .orElse("N/A");
    }
    
    private double calculateJobProgress(JobExecution execution) {
        Collection<StepExecution> steps = execution.getStepExecutions();
        if (steps.isEmpty()) return 0.0;
        
        long completedSteps = steps.stream()
            .filter(s -> s.getStatus() == BatchStatus.COMPLETED)
            .count();
        
        return (double) completedSteps / steps.size() * 100;
    }
    
    /**
     * 특정 작업 실행의 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public JobExecution getJobExecutionDetail(Long executionId) {
        return jobExplorer.getJobExecution(executionId);
    }
    
    /**
     * 실패한 아이템 목록 조회
     */
    @Transactional(readOnly = true)
    public List<TmdbWorkQueue> getFailedItems(int page, int size) {
        return workQueueRepository.findByStatus(
            TmdbWorkQueue.ProcessStatus.FAILED,
            org.springframework.data.domain.PageRequest.of(page, size)
        ).getContent();
    }
}