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

@Entity(name = "SyncCollection")
@Table(name = "collection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Collection extends BaseEntity {

    @Id
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(length = 1024)
    private String name;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Builder
    public Collection(Long tmdbId, String name, String posterPath, String backdropPath) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }
}