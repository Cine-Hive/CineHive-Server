package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity(name = "SyncEpisodeVideo")
@Table(name = "episode_video")
@IdClass(EpisodeVideoId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EpisodeVideo {

    @Id
    @Column(name = "episode_tmdb_id")
    private Long episodeTmdbId;

    @Id
    @Column(name = "video_key", length = 64)
    private String videoKey;

    @Column(length = 64)
    private String site;

    @Column(length = 64)
    private String type;

    @Column(name = "iso_639_1", length = 8)
    private String iso6391;

    private boolean official;

    @Column(name = "published_at")
    private ZonedDateTime publishedAt;

    @Column(length = 1024)
    private String name;

    @Builder
    public EpisodeVideo(Long episodeTmdbId, String videoKey, String site, String type, String iso6391,
                        boolean official, ZonedDateTime publishedAt, String name) {
        this.episodeTmdbId = episodeTmdbId;
        this.videoKey = videoKey;
        this.site = site;
        this.type = type;
        this.iso6391 = iso6391;
        this.official = official;
        this.publishedAt = publishedAt;
        this.name = name;
    }
}