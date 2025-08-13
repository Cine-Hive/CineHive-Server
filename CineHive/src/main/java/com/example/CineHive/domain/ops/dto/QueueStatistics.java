package com.example.CineHive.domain.ops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatistics {
    
    private long moviePending;
    private long movieProcessed;
    private long tvPending;
    private long tvProcessed;
    private long personPending;
    private long personProcessed;
    
    public long getTotalPending() {
        return moviePending + tvPending + personPending;
    }
    
    public long getTotalProcessed() {
        return movieProcessed + tvProcessed + personProcessed;
    }
    
    public long getTotal() {
        return getTotalPending() + getTotalProcessed();
    }
    
    public double getCompletionRate() {
        long total = getTotal();
        if (total == 0) {
            return 0.0;
        }
        return (double) getTotalProcessed() / total * 100.0;
    }
}