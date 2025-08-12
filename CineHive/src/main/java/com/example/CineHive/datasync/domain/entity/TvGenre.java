package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvGenre")
@Table(name = "tv_genre")
@IdClass(TvGenreId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvGenre {
    @Id
    @Column(name = "tv_id")
    private Long tvId;

    @Id
    @Column(name = "genre_id")
    private Long genreId;

    @Builder
    public TvGenre(Long tvId, Long genreId) {
        this.tvId = tvId;
        this.genreId = genreId;
    }
}