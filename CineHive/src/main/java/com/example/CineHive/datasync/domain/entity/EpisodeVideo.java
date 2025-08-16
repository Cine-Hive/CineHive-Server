package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    
    /**
     * TMDB API 응답을 EpisodeVideo 엔티티로 변환하는 static factory 메서드
     */
    public static EpisodeVideo fromTmdbResponse(Long episodeTmdbId, com.example.CineHive.client.tmdb.dto.TmdbVideoResponse response) {
        ZonedDateTime publishedAt = null;
        if (response.publishedAt() != null && !response.publishedAt().isBlank()) {
            try {
                publishedAt = ZonedDateTime.parse(response.publishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e) {
                // 파싱 실패 시 null 처리
            }
        }
        
        return EpisodeVideo.builder()
                .episodeTmdbId(episodeTmdbId)
                .videoKey(response.key())
                .site(response.site())
                .type(response.type())
                .iso6391(response.iso6391())
                .official(response.official())
                .publishedAt(publishedAt)
                .name(response.name())
                .build();
    }
}