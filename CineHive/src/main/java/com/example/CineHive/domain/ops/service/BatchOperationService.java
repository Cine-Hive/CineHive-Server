package com.example.CineHive.domain.ops.service;

import com.example.CineHive.domain.ops.dto.BatchExecutionResponse;
import com.example.CineHive.domain.ops.dto.BatchStatusResponse;
import com.example.CineHive.domain.ops.dto.QueueStatistics;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.repository.TmdbWorkQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job fullSyncJob;
    private final TmdbWorkQueueRepository tmdbWorkQueueRepository;

    public BatchExecutionResponse startFullSyncJob(String fileDate) {
        try {
            // 새 JobInstance를 위한 고유한 JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fileDate", fileDate)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(fullSyncJob, jobParameters);
            
            return BatchExecutionResponse.success(
                jobExecution.getId(),
                jobExecution.getStatus(),
                "Full Sync Job이 시작되었습니다."
            );
            
        } catch (JobExecutionAlreadyRunningException e) {
            return BatchExecutionResponse.error("이미 실행 중인 Job이 있습니다.");
        } catch (JobRestartException e) {
            return BatchExecutionResponse.error("Job 재시작 실패: " + e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            return BatchExecutionResponse.error("해당 매개변수로 이미 완료된 Job이 있습니다.");
        } catch (JobParametersInvalidException e) {
            return BatchExecutionResponse.error("잘못된 Job 매개변수: " + e.getMessage());
        } catch (Exception e) {
            log.error("Full Sync Job 실행 실패", e);
            return BatchExecutionResponse.error("Job 실행 실패: " + e.getMessage());
        }
    }

    public BatchStatusResponse getBatchStatus(Long jobExecutionId) {
        try {
            JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
            
            if (jobExecution == null) {
                return BatchStatusResponse.error("Job Execution을 찾을 수 없습니다: " + jobExecutionId);
            }

            return BatchStatusResponse.success(jobExecution);
            
        } catch (Exception e) {
            log.error("배치 상태 조회 실패: jobExecutionId={}", jobExecutionId, e);
            return BatchStatusResponse.error("상태 조회 실패: " + e.getMessage());
        }
    }

    public QueueStatistics getQueueStatistics() {
        long moviePending = tmdbWorkQueueRepository.countUnprocessedByEntityType(TmdbWorkQueue.EntityType.MOVIE);
        long movieProcessed = tmdbWorkQueueRepository.countProcessedByEntityType(TmdbWorkQueue.EntityType.MOVIE);
        
        return QueueStatistics.builder()
                .moviePending(moviePending)
                .movieProcessed(movieProcessed)
                .build();
    }

    @Transactional
    public int clearFailedQueueItems() {
        // 실패 횟수가 3회 이상인 아이템들을 제거
        return tmdbWorkQueueRepository.deleteByAttemptsGreaterThanEqual(3);
    }
}