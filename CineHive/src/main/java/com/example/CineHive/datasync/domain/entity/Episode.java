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

@Entity(name = "SyncEpisode")
@Table(name = "episode")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Episode extends BaseEntity {

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
    
    /**
     * TMDB API 응답을 Episode 엔티티로 변환하는 static factory 메서드
     * 주의: 이 메서드는 간단한 에피소드 정보만 처리함. 상세 정보는 별도 API 호출 필요
     */
    public static Episode fromTmdbResponse(Long tvTmdbId, Long seasonTmdbId, 
                                          com.example.CineHive.client.tmdb.dto.TmdbEpisodeSimpleResponse response) {
        return Episode.builder()
                .tmdbId(response.id())
                .tvTmdbId(tvTmdbId)
                .seasonTmdbId(seasonTmdbId)
                .seasonNumber(response.seasonNumber())
                .episodeNumber(response.episodeNumber())
                .name(response.name())
                .overview(response.overview())
                .airDate(parseDate(response.airDate()))
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