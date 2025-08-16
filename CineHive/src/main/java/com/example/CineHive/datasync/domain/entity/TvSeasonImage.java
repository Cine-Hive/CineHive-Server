package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity(name = "SyncTvSeasonImage")
@Table(name = "tv_season_image")
@IdClass(TvSeasonImageId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvSeasonImage {

    @Id
    @Column(name = "season_tmdb_id")
    private Long seasonTmdbId;

    @Id
    @Column(name = "file_path")
    private String filePath;

    @Column(name = "iso_639_1", length = 8)
    private String iso6391;

    @Column(name = "aspect_ratio", precision = 8, scale = 4)
    private BigDecimal aspectRatio;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    private Integer width;
    private Integer height;

    @Builder
    public TvSeasonImage(Long seasonTmdbId, String filePath, String iso6391, BigDecimal aspectRatio,
                         BigDecimal voteAverage, Integer voteCount, Integer width, Integer height) {
        this.seasonTmdbId = seasonTmdbId;
        this.filePath = filePath;
        this.iso6391 = iso6391;
        this.aspectRatio = aspectRatio;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.width = width;
        this.height = height;
    }
    
    /**
     * TMDB API 응답을 TvSeasonImage 엔티티로 변환하는 static factory 메서드
     */
    public static TvSeasonImage fromTmdbResponse(Long seasonTmdbId, com.example.CineHive.client.tmdb.dto.TmdbImageResponse response) {
        return TvSeasonImage.builder()
                .seasonTmdbId(seasonTmdbId)
                .filePath(response.filePath())
                .iso6391(response.iso_639_1())
                .aspectRatio(response.aspectRatio() != null ? BigDecimal.valueOf(response.aspectRatio()) : null)
                .voteAverage(response.voteAverage() != null ? BigDecimal.valueOf(response.voteAverage()) : null)
                .voteCount(response.voteCount())
                .width(response.width())
                .height(response.height())
                .build();
    }
}