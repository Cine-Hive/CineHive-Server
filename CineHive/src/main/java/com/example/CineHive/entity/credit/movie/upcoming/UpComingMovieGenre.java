package com.example.CineHive.entity.credit.movie.upcoming;

import com.example.CineHive.entity.videotype.UpComingMovie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="upcoming_movie_genres")
public class UpComingMovieGenre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private UpComingMovie movie;

}
