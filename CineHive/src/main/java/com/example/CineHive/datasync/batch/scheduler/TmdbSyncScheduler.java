package com.example.CineHive.datasync.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TMDB 동기화 배치 작업 스케줄러
 * 정기적으로 배치 작업을 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "prod"}) // local 프로파일에서는 실행하지 않음
public class TmdbSyncScheduler {

    private final JobLauncher jobLauncher;
    
    @Qualifier("fullSyncJob")
    private final Job fullSyncJob;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 매일 새벽 2시에 전체 동기화 실행
     * 새로운 영화/TV 시리즈 정보를 가져옴
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyFullSync() {
        log.info("일일 전체 동기화 시작: {}", LocalDateTime.now().format(formatter));
        runBatchJob(fullSyncJob, "Daily Full Sync");
    }
    
    /**
     * 매주 일요일 새벽 3시에 전체 재동기화 실행
     * 기존 데이터도 모두 업데이트
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void runWeeklyResync() {
        log.info("주간 재동기화 시작: {}", LocalDateTime.now().format(formatter));
        
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("syncType", "FULL_RESYNC")
                .addString("scheduleType", "Weekly Resync")
                .toJobParameters();
        
        runBatchJobWithParams(fullSyncJob, params);
    }
    
    /**
     * 6시간마다 실패한 항목 재시도
     * FAILED 상태인 항목들을 다시 처리
     */
    @Scheduled(fixedDelay = 21600000) // 6시간 = 21600000ms
    public void retryFailedItems() {
        log.info("실패 항목 재시도 시작: {}", LocalDateTime.now().format(formatter));
        
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("syncType", "RETRY_FAILED")
                .addString("scheduleType", "Failed Items Retry")
                .toJobParameters();
        
        runBatchJobWithParams(fullSyncJob, params);
    }
    
    /**
     * 배치 작업 실행 헬퍼 메서드
     */
    private void runBatchJob(Job job, String description) {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("scheduleType", description)
                .toJobParameters();
        
        runBatchJobWithParams(job, params);
    }
    
    /**
     * 파라미터를 포함한 배치 작업 실행
     */
    private void runBatchJobWithParams(Job job, JobParameters params) {
        try {
            JobExecution execution = jobLauncher.run(job, params);
            log.info("배치 작업 시작됨 - Job ID: {}, Status: {}", 
                    execution.getId(), execution.getStatus());
            
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("배치 작업이 이미 실행 중입니다: {}", e.getMessage());
        } catch (JobRestartException e) {
            log.error("배치 작업을 재시작할 수 없습니다: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.info("배치 작업이 이미 완료되었습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("배치 작업 실행 실패", e);
        }
    }
    
    /**
     * 수동 실행 메서드 (관리자 API에서 호출 가능)
     */
    public void runManualSync(String syncType) {
        log.info("수동 동기화 시작 - Type: {}, Time: {}", syncType, LocalDateTime.now().format(formatter));
        
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("syncType", syncType)
                .addString("scheduleType", "Manual")
                .addString("triggeredBy", "Admin")
                .toJobParameters();
        
        runBatchJobWithParams(fullSyncJob, params);
    }
}