package com.example.CineHive.datasync.domain.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TmdbWorkQueueId implements Serializable {
    private String entityType;
    private Long tmdbId;
}