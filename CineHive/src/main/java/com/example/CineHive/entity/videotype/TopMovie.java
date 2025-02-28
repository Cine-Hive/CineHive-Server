package com.example.CineHive.entity.videotype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Topmovie")
public class TopMovie {
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


}
