package com.example.CineHive.datasync.domain.entity;

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
public class ProductionCompany {

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
}