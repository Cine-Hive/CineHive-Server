package com.example.CineHive.entity.videotype;

import com.example.CineHive.entity.credit.drama.Actor;
import com.example.CineHive.entity.credit.drama.Director;
import com.example.CineHive.entity.credit.drama.Genre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate; // LocalDate 추가
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drama")
public class Drama {
    @Id
    private Long id;
    private String name;

    @Lob
    private String overview;
    private String posterPath;
    private String backDropPath;
    @Column(name = "first_air_date")
    private String firstAirDate;
    private double voteAverage;
    private double popularity;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id")
    private List<Director> directors;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id")
    private List<Genre> genres = new ArrayList<>();


    @OneToMany(mappedBy = "drama", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Actor> actors = new ArrayList<>();
}
