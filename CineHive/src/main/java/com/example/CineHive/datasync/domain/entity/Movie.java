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

@Entity(name = "SyncMovie")
@Table(name = "movie")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String title;

    @Column(name = "original_title", length = 1024)
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "original_language", length = 8)
    private String originalLanguage;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private Integer runtime;

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
    public Movie(Long tmdbId, String title, String originalTitle, String overview, String originalLanguage,
                 LocalDate releaseDate, Integer runtime, String status, String posterPath, String backdropPath,
                 BigDecimal popularity, BigDecimal voteAverage, Integer voteCount, ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.originalLanguage = originalLanguage;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.status = status;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }
}