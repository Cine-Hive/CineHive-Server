package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Entity(name = "SyncTvSeries")
@Table(name = "tv_series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeries extends BaseEntity {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String name;

    @Column(name = "original_name", length = 1024)
    private String originalName;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(columnDefinition = "TEXT")
    private String tagline;

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

    @Column(length = 64)
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "created_by_json", columnDefinition = "jsonb")
    private String createdByJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_episode_to_air_json", columnDefinition = "jsonb")
    private String lastEpisodeToAirJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "next_episode_to_air_json", columnDefinition = "jsonb")
    private String nextEpisodeToAirJson;

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

    @Column(name = "soft_deleted")
    private boolean softDeleted = false;

    @Column(name = "soft_deleted_at")
    private ZonedDateTime softDeletedAt;

    @Column(name = "updated_from_tmdb_at")
    private ZonedDateTime updatedFromTmdbAt;

    @Builder
    public TvSeries(Long tmdbId, String name, String originalName, String overview, String tagline,
                    String originalLanguage, LocalDate firstAirDate, LocalDate lastAirDate,
                    Integer numberOfSeasons, Integer numberOfEpisodes, boolean inProduction,
                    String status, String type, String createdByJson, String lastEpisodeToAirJson,
                    String nextEpisodeToAirJson, String posterPath, String backdropPath,
                    BigDecimal popularity, BigDecimal voteAverage, Integer voteCount,
                    ZonedDateTime updatedFromTmdbAt) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.originalName = originalName;
        this.overview = overview;
        this.tagline = tagline;
        this.originalLanguage = originalLanguage;
        this.firstAirDate = firstAirDate;
        this.lastAirDate = lastAirDate;
        this.numberOfSeasons = numberOfSeasons;
        this.numberOfEpisodes = numberOfEpisodes;
        this.inProduction = inProduction;
        this.status = status;
        this.type = type;
        this.createdByJson = createdByJson;
        this.lastEpisodeToAirJson = lastEpisodeToAirJson;
        this.nextEpisodeToAirJson = nextEpisodeToAirJson;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.updatedFromTmdbAt = updatedFromTmdbAt;
    }
    
    /**
     * TMDB API 응답을 TvSeries 엔티티로 변환하는 static factory 메서드
     */
    public static TvSeries fromTmdbResponse(com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        String createdByJson = null;
        String lastEpisodeJson = null;
        String nextEpisodeJson = null;
        
        try {
            if (response.createdBy() != null && !response.createdBy().isEmpty()) {
                createdByJson = objectMapper.writeValueAsString(response.createdBy());
            }
            if (response.lastEpisodeToAir() != null) {
                lastEpisodeJson = objectMapper.writeValueAsString(response.lastEpisodeToAir());
            }
            if (response.nextEpisodeToAir() != null) {
                nextEpisodeJson = objectMapper.writeValueAsString(response.nextEpisodeToAir());
            }
        } catch (JsonProcessingException e) {
            // 로그 처리는 실제 구현시 추가
        }
        
        return TvSeries.builder()
                .tmdbId(response.id())
                .name(response.name())
                .originalName(response.originalName())
                .overview(response.overview())
                .tagline(response.tagline())
                .firstAirDate(parseDate(response.firstAirDate()))
                .lastAirDate(parseDate(response.lastAirDate()))
                .numberOfSeasons(response.numberOfSeasons())
                .numberOfEpisodes(response.numberOfEpisodes())
                .inProduction(response.inProduction())
                .status(response.status())
                .type(response.type())
                .createdByJson(createdByJson)
                .lastEpisodeToAirJson(lastEpisodeJson)
                .nextEpisodeToAirJson(nextEpisodeJson)
                .posterPath(response.posterPath())
                .backdropPath(response.backdropPath())
                .popularity(response.popularity())
                .voteAverage(response.voteAverage())
                .voteCount(response.voteCount())
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