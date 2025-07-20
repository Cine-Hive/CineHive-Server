package com.example.CineHive.entity.media;

import com.example.CineHive.entity.BaseEntity;
import com.example.CineHive.dto.media.MediaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Media extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String title;

    private String posterPath;

    private String releaseDate;

    @Builder
    public Media(Long tmdbId, MediaType mediaType, String title, String posterPath, String releaseDate) {
        this.tmdbId = tmdbId;
        this.mediaType = mediaType;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
    }
}