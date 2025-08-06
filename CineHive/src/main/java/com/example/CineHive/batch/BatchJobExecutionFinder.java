package com.example.CineHive.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Spring Batch의 JobExecution 메타데이터를 조회하는 유틸리티 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class BatchJobExecutionFinder {

    private final JobExplorer jobExplorer;

    /**
     * 주어진 Job 이름으로 가장 최근에 'COMPLETED'된 JobExecution의 종료 시각을 찾습니다.
     * @param jobName 조회할 Job의 이름
     * @return 마지막 성공 실행의 종료 시각 (Optional)
     */
    public Optional<LocalDateTime> findLastSuccessfulJobEndTime(String jobName) {
        List<Long> jobInstanceIds = jobExplorer.getJobInstances(jobName, 0, 1)
                .stream()
                .map(jobInstance -> jobInstance.getInstanceId())
                .toList();

        if (jobInstanceIds.isEmpty()) {
            return Optional.empty(); // 한 번도 실행된 적 없음
        }

        JobExecution lastJobExecution = jobExplorer.getJobExecution(jobInstanceIds.get(0));
        if (lastJobExecution == null) {
            return Optional.empty();
        }

        return jobExplorer.getJobExecutions(lastJobExecution.getJobInstance()).stream()
                .filter(execution -> execution.getStatus() == BatchStatus.COMPLETED)
                .map(JobExecution::getEndTime)
                .max(Comparator.naturalOrder());
    }
}