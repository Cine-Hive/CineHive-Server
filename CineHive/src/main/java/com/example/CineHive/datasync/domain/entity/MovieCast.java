package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieCast")
@Table(name = "movie_cast")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieCast {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "character_name", length = 512)
    private String characterName;

    @Column(name = "cast_order")
    private Integer castOrder;

    @Builder
    public MovieCast(String creditId, Long movieId, Long personId, String characterName, Integer castOrder) {
        this.creditId = creditId;
        this.movieId = movieId;
        this.personId = personId;
        this.characterName = characterName;
        this.castOrder = castOrder;
    }
}