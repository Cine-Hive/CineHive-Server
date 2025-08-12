package com.example.CineHive.domain.collection.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "collections")
public class Collection extends BaseEntity {

    @Id
    @Column(name = "collection_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Builder
    public Collection(Long id, String name, String posterPath, String backdropPath) {
        this.id = id;
        this.name = name;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
    }
}