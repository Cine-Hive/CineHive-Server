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

@Entity(name = "SyncProductionCompany")
@Table(name = "production_company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionCompany extends BaseEntity {

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
    public ProductionCompany(Long tmdbId, String name, String logoPath, String originCountry) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.logoPath = logoPath;
        this.originCountry = originCountry;
    }

    /**
     * TMDB API 응답으로부터 ProductionCompany 엔티티를 생성하는 static factory 메서드
     */
    public static ProductionCompany fromTmdbResponse(com.example.CineHive.client.tmdb.dto.TmdbProductionCompanyResponse response) {
        return ProductionCompany.builder()
                .tmdbId(response.id())
                .name(response.name())
                .logoPath(response.logoPath())
                .originCountry(response.originCountry())
                .build();
    }
}