package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieCrew")
@Table(name = "movie_crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieCrew extends BaseEntity {

    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(nullable = false)
    private String job;

    private String department;

    @Builder
    public MovieCrew(String creditId, Long movieId, Long personId, String job, String department) {
        this.creditId = creditId;
        this.movieId = movieId;
        this.personId = personId;
        this.job = job;
        this.department = department;
    }
}