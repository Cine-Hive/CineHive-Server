package com.example.CineHive.domain.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionResponse {
    
    private boolean success;
    private Long jobExecutionId;
    private BatchStatus status;
    private String message;
    private String errorMessage;

    public static BatchExecutionResponse success(Long jobExecutionId, BatchStatus status, String message) {
        return new BatchExecutionResponse(true, jobExecutionId, status, message, null);
    }

    public static BatchExecutionResponse error(String errorMessage) {
        return new BatchExecutionResponse(false, null, null, null, errorMessage);
    }
}