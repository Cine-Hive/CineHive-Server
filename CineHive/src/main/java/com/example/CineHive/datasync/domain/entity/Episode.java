package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity(name = "SyncEpisode")
@Table(name = "episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "tv_tmdb_id", nullable = false)
    private Long tvTmdbId;

    @Column(name = "season_tmdb_id", nullable = false)
    private Long seasonTmdbId;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(length = 1024)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "air_date")
    private LocalDate airDate;

    private Integer runtime;

    @Column(name = "still_path")
    private String stillPath;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public Episode(Long tmdbId, Long tvTmdbId, Long seasonTmdbId, Integer seasonNumber, Integer episodeNumber,
                   String name, String overview, LocalDate airDate, Integer runtime, String stillPath,
                   BigDecimal voteAverage, Integer voteCount, ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.tvTmdbId = tvTmdbId;
        this.seasonTmdbId = seasonTmdbId;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.name = name;
        this.overview = overview;
        this.airDate = airDate;
        this.runtime = runtime;
        this.stillPath = stillPath;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }
}