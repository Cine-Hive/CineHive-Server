package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Entity
@Table(name = "ref_tmdb_configuration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmdbConfiguration {

    @Id
    private Short id = 1;

    @Column(nullable = false)
    private String secureBaseUrl;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private ZonedDateTime fetchedAt;

    @Builder
    public TmdbConfiguration(String secureBaseUrl, String baseUrl, ZonedDateTime fetchedAt) {
        this.secureBaseUrl = secureBaseUrl;
        this.baseUrl = baseUrl;
        this.fetchedAt = fetchedAt;
    }
}