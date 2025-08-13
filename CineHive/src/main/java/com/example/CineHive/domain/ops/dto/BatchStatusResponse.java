package com.example.CineHive.domain.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatusResponse {
    
    private boolean success;
    private Long jobExecutionId;
    private BatchStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String jobName;
    private List<StepStatus> stepStatuses;
    private String errorMessage;

    @Getter
    @AllArgsConstructor
    public static class StepStatus {
        private String stepName;
        private BatchStatus status;
        private int readCount;
        private int writeCount;
        private int skipCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    public static BatchStatusResponse success(JobExecution jobExecution) {
        List<StepStatus> stepStatuses = jobExecution.getStepExecutions().stream()
                .map(stepExecution -> new StepStatus(
                    stepExecution.getStepName(),
                    stepExecution.getStatus(),
                    (int) stepExecution.getReadCount(),
                    (int) stepExecution.getWriteCount(),
                    (int) stepExecution.getSkipCount(),
                    stepExecution.getStartTime(),
                    stepExecution.getEndTime()
                ))
                .collect(java.util.stream.Collectors.toList());

        return new BatchStatusResponse(
            true,
            jobExecution.getId(),
            jobExecution.getStatus(),
            jobExecution.getStartTime(),
            jobExecution.getEndTime(),
            jobExecution.getJobInstance().getJobName(),
            stepStatuses,
            null
        );
    }

    public static BatchStatusResponse error(String errorMessage) {
        return new BatchStatusResponse(false, null, null, null, null, null, null, errorMessage);
    }
}