package com.example.CineHive.domain.media;

import com.example.CineHive.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    public void updateRating(double totalRating, int newReviewCount) {
        this.reviewCount = newReviewCount;
        if (newReviewCount > 0) {
            this.averageRating = Math.round((totalRating / newReviewCount) * 100.0) / 100.0;
        } else {
            this.averageRating = 0.0;
        }
    }
}