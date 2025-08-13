package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieGenre")
@Table(name = "movie_genre")
@IdClass(MovieGenreId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieGenre extends BaseEntity {
    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Id
    @Column(name = "genre_id")
    private Long genreId;

    @Builder
    public MovieGenre(Long movieId, Long genreId) {
        this.movieId = movieId;
        this.genreId = genreId;
    }
}