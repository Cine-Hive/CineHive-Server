package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity(name = "SyncEpisodeImage")
@Table(name = "episode_image")
@IdClass(EpisodeImageId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeImage {

    @Id
    @Column(name = "episode_tmdb_id")
    private Long episodeTmdbId;

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
    public EpisodeImage(Long episodeTmdbId, String filePath, String iso6391, BigDecimal aspectRatio,
                        BigDecimal voteAverage, Integer voteCount, Integer width, Integer height) {
        this.episodeTmdbId = episodeTmdbId;
        this.filePath = filePath;
        this.iso6391 = iso6391;
        this.aspectRatio = aspectRatio;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.width = width;
        this.height = height;
    }
}