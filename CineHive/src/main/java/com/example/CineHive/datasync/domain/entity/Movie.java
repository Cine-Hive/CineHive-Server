package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
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
public class Movie extends BaseEntity {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String title;

    @Column(name = "original_title", length = 1024)
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(columnDefinition = "TEXT")
    private String tagline;

    @Column(name = "original_language", length = 8)
    private String originalLanguage;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private Integer runtime;

    @Column(length = 64)
    private String status;

    private Long budget;
    private Long revenue;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(precision = 10, scale = 4)
    private BigDecimal popularity;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "collection_id")
    private Long collectionId; // 추가된 필드

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    @Column(name = "soft_deleted_at")
    private ZonedDateTime softDeletedAt;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public Movie(Long tmdbId, String title, String originalTitle, String overview, String tagline, String originalLanguage,
                 LocalDate releaseDate, Integer runtime, String status, Long budget, Long revenue, String posterPath,
                 String backdropPath, BigDecimal popularity, BigDecimal voteAverage, Integer voteCount,
                 Long collectionId, ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.tagline = tagline;
        this.originalLanguage = originalLanguage;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.status = status;
        this.budget = budget;
        this.revenue = revenue;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.collectionId = collectionId;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }

    /**
     * TMDB API 응답으로부터 Movie 엔티티를 생성하는 static factory 메서드
     */
    public static Movie fromTmdbResponse(com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse response) {
        return Movie.builder()
                .tmdbId(response.id())
                .title(response.title())
                .originalTitle(response.originalTitle())
                .overview(response.overview())
                .tagline(response.tagline())
                .releaseDate(parseDate(response.releaseDate()))
                .runtime(response.runtime())
                .status(response.status())
                .budget(response.budget())
                .revenue(response.revenue())
                .posterPath(response.posterPath())
                .backdropPath(response.backdropPath())
                .popularity(toBigDecimal(response.popularity()))
                .voteAverage(toBigDecimal(response.voteAverage()))
                .voteCount(response.voteCount())
                .collectionId(response.collection() != null ? response.collection().id() : null)
                .updatedFromTmdbAt(ZonedDateTime.now())
                .build();
    }

    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }
}