package com.example.CineHive.entity.videotype;

import com.example.CineHive.entity.credit.movie.UpComingGenre;
import com.example.CineHive.entity.credit.movie.topMovieGenre;
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
@Table(name="UpComingMovie")
public class UpComingMovie {
    @Id
    private Long id;

    private String title;
    @Lob
    private String overview;

    private String posterPath;

    private String backDropPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private List<UpComingGenre> genres = new ArrayList<>();

    private double voteAverage;

    private double popularity;

    private int runtime;

}
