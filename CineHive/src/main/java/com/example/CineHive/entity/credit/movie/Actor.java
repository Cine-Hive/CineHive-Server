package com.example.CineHive.entity.credit.movie;

import com.example.CineHive.entity.videotype.Movie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "movie_actors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Actor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String posterPath; // 포스터 이미지 URL 추가

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
}