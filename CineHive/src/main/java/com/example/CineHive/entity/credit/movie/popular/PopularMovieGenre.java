package com.example.CineHive.entity.credit.movie.popular;

import com.example.CineHive.entity.videotype.PoPularMovie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="popular_movie_genres")
public class PopularMovieGenre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private PoPularMovie movie;

}
