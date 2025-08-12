package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity(name = "SyncTvSeries")
@Table(name = "tv_series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeries {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String name;

    @Column(name = "original_name", length = 1024)
    private String originalName;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "original_language", length = 8)
    private String originalLanguage;

    @Column(name = "first_air_date")
    private LocalDate firstAirDate;

    @Column(name = "last_air_date")
    private LocalDate lastAirDate;

    private Integer numberOfSeasons;
    private Integer numberOfEpisodes;
    private boolean inProduction;

    @Column(length = 64)
    private String status;

    @Column(name = "poster_path", length = 255)
    private String posterPath;

    @Column(name = "backdrop_path", length = 255)
    private String backdropPath;

    @Column(precision = 10, scale = 4)
    private BigDecimal popularity;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    private boolean softDeleted = false;

    @Column(name = "soft_deleted_at")
    private ZonedDateTime softDeletedAt;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public TvSeries(Long tmdbId, String name, String originalName, String overview, String originalLanguage,
                    LocalDate firstAirDate, LocalDate lastAirDate, Integer numberOfSeasons, Integer numberOfEpisodes,
                    boolean inProduction, String status, String posterPath, String backdropPath, BigDecimal popularity,
                    BigDecimal voteAverage, Integer voteCount, ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.originalName = originalName;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
        this.lastAirDate = lastAirDate;
        this.numberOfSeasons = numberOfSeasons;
        this.numberOfEpisodes = numberOfEpisodes;
        this.inProduction = inProduction;
        this.status = status;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }
}