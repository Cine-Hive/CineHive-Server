package com.example.CineHive.entity.credit.movie;

import com.example.CineHive.entity.videotype.TopMovie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="top_movie_genres")
public class topMovieGenre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private TopMovie movie;

}