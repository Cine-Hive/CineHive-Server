package com.example.CineHive.entity.videotype;

import com.example.CineHive.entity.credit.movie.Actor;
import com.example.CineHive.entity.credit.movie.Director;
import com.example.CineHive.entity.credit.movie.Video;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie")
public class Movie {
    @Id
    private Long id;

    private String title;
    @Lob
    private String overview;

    private String posterPath;
    private String backDropPath;
    @Column(name = "release_date")
    private LocalDate releaseDate;
    @ElementCollection
    private List<Integer> genreIds;
    private double voteAverage;
    private double popularity;
    // 출연진 정보
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actor> actors = new ArrayList<>();

    public void addActor(Actor actor) {
        actors.add(actor);
        actor.setMovie(this);
    }

    // 비디오 정보를 저장하는 리스트
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private List<Video> videos;

    // 감독 정보
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "director_id")
    private Director director;

    // genres → 현재 영화의 장르정보 추가 필요
    // homepage → OTT 연동 추가 필요
    // runtime → 상영시간 추가 필요
    // production_companies → 배급사 정보와 배급사 로고 추가 필요

    // 현재 MovieDetail에서 불러와지는 Actor(배우) 이미지의 변수명이 영화와 통일성없이 PosterUrl로 넘어옴
    //    → 영화와 같이 PosterPath로 개선 필요
}
