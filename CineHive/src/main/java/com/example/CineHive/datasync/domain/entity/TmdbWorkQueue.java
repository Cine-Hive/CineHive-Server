package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity(name = "SyncTmdbWorkQueue")
@Table(name = "tmdb_work_queue")
@IdClass(TmdbWorkQueueId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmdbWorkQueue {

    @Id
    private String entityType;

    @Id
    private Long tmdbId;

    private int priority;
    private int attempts;
    private ZonedDateTime enqueuedAt;

    @Builder
    public TmdbWorkQueue(String entityType, Long tmdbId, int priority) {
        this.entityType = entityType;
        this.tmdbId = tmdbId;
        this.priority = priority;
        this.attempts = 0;
        this.enqueuedAt = ZonedDateTime.now();
    }
}