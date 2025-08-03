package com.example.CineHive.domain.media.controller.entity;

import com.example.CineHive.client.tmdb.dto.TmdbMovieDetailResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesDetailResponse;
import com.example.CineHive.domain.common.controller.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "media",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_media_tmdb_id_type",
                        columnNames = {"tmdbId", "mediaType"}
                )
        }
)
public class Media extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(nullable = false)
    private String title;

    private String posterPath;

    private String releaseDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "media_genres", joinColumns = @JoinColumn(name = "media_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(nullable = false)
    private int reviewCount = 0;

    @Builder
    public Media(Integer tmdbId, MediaType mediaType, String title, String posterPath, String releaseDate, Set<Genre> genres) {
        this.tmdbId = tmdbId;
        this.mediaType = mediaType;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.genres = genres != null ? genres : new HashSet<>();
    }

    public static Media from(TmdbMovieDetailResponse tmdb) {
        return Media.builder()
                .tmdbId(tmdb.id().intValue())
                .mediaType(MediaType.MOVIE)
                .title(tmdb.title())
                .posterPath(tmdb.posterPath())
                .releaseDate(tmdb.releaseDate())
                .genres(tmdb.genres().stream()
                        .map(g -> Genre.fromTmdbId(g.id()))
                        .flatMap(java.util.Optional::stream)
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Media from(TmdbTvSeriesDetailResponse tmdb) {
        return Media.builder()
                .tmdbId(tmdb.id().intValue())
                .mediaType(MediaType.TV)
                .title(tmdb.name())
                .posterPath(tmdb.posterPath())
                .releaseDate(tmdb.firstAirDate())
                .genres(tmdb.genres().stream()
                        .map(g -> Genre.fromTmdbId(g.id()))
                        .flatMap(java.util.Optional::stream)
                        .collect(Collectors.toSet()))
                .build();
    }

    public void updateRating(double averageRating, int newReviewCount) {
        this.reviewCount = newReviewCount;
        this.averageRating = averageRating;
    }
}