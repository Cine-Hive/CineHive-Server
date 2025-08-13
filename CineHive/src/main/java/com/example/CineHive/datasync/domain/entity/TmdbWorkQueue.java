package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "TmdbWorkQueue")
@Table(name = "tmdb_work_queue",
       uniqueConstraints = @UniqueConstraint(columnNames = {"entity_type", "tmdb_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmdbWorkQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;
    
    @Column(name = "status", length = 20)
    private String status = "READY";

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum EntityType {
        MOVIE("movie"),
        TV("tv"),
        PERSON("person");

        private final String value;

        EntityType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public TmdbWorkQueue(EntityType entityType, Long tmdbId, Integer priority) {
        this.entityType = entityType;
        this.tmdbId = tmdbId;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
        this.status = "DONE";
        this.attempts++;
    }

    public void markAsFailed(String error) {
        this.attempts++;
        this.lastError = error;
        this.status = "FAILED";
    }
    
    public void markAsSkipped() {
        this.processed = true;
        this.status = "SKIPPED";
        this.processedAt = LocalDateTime.now();
    }

    public boolean isProcessed() {
        return processed;
    }

    public boolean hasErrors() {
        return lastError != null;
    }
}