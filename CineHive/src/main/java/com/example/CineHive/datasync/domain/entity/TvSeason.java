package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity(name = "SyncTvSeason")
@Table(name = "tv_season")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeason {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "tv_tmdb_id", nullable = false)
    private Long tvTmdbId;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @Column(length = 1024)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "air_date")
    private LocalDate airDate;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public TvSeason(Long tmdbId, Long tvTmdbId, Integer seasonNumber, String name, String overview,
                    LocalDate airDate, Integer episodeCount, String posterPath, BigDecimal voteAverage,
                    ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.tvTmdbId = tvTmdbId;
        this.seasonNumber = seasonNumber;
        this.name = name;
        this.overview = overview;
        this.airDate = airDate;
        this.episodeCount = episodeCount;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }
}