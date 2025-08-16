package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncNetwork")
@Table(name = "network")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Network extends BaseEntity {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String name;

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "origin_country", length = 8)
    private String originCountry;

    @Builder
    public Network(Long tmdbId, String name, String logoPath, String originCountry) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.logoPath = logoPath;
        this.originCountry = originCountry;
    }
    
    /**
     * TMDB API 응답을 Network 엔티티로 변환하는 static factory 메서드
     */
    public static Network fromTmdbResponse(com.example.CineHive.client.tmdb.dto.TmdbNetworkResponse response) {
        return Network.builder()
                .tmdbId(response.id())
                .name(response.name())
                .logoPath(response.logoPath())
                .originCountry(response.originCountry())
                .build();
    }
}