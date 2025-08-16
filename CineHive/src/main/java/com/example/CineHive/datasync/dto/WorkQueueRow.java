package com.example.CineHive.datasync.dto;

/**
 * DTO for JDBC-based reading of tmdb_work_queue
 */
public record WorkQueueRow(
    String entityType,
    Long tmdbId,
    Integer priority,
    boolean processed
) {
}