package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity(name = "SyncTvSeason")
@Table(name = "tv_season")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeason extends BaseEntity {

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
    
    /**
     * TMDB API 응답을 TvSeason 엔티티로 변환하는 static factory 메서드
     */
    public static TvSeason fromTmdbResponse(Long tvTmdbId, com.example.CineHive.client.tmdb.dto.TmdbSeasonResponse response) {
        return TvSeason.builder()
                .tmdbId(response.id())
                .tvTmdbId(tvTmdbId)
                .seasonNumber(response.seasonNumber())
                .name(response.name())
                .overview(response.overview())
                .airDate(parseDate(response.airDate()))
                .episodeCount(response.episodeCount())
                .posterPath(response.posterPath())
                .voteAverage(response.voteAverage())
                .updatedFromTmdbAt(ZonedDateTime.now())
                .build();
    }
    
    /**
     * 날짜 문자열을 LocalDate로 파싱하는 유틸리티 메서드
     */
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}