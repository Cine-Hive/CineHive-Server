package com.example.CineHive.datasync.domain.entity;

import com.example.CineHive.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncMovieCrew")
@Table(name = "movie_crew",
    uniqueConstraints = @UniqueConstraint(columnNames = {"credit_id", "movie_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovieCrew extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "credit_id", length = 32, nullable = false)
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

    /**
     * TMDB API 응답으로부터 MovieCrew 엔티티를 생성하는 static factory 메서드
     */
    public static MovieCrew fromTmdbResponse(Long movieId, com.example.CineHive.client.tmdb.dto.TmdbMediaCrewResponse response) {
        return MovieCrew.builder()
                .creditId(response.creditId())
                .movieId(movieId)
                .personId(response.id())
                .job(response.job())
                .department(response.department())
                .build();
    }
}