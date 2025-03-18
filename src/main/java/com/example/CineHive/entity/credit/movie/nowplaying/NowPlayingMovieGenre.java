package com.example.CineHive.entity.credit.movie.nowplaying;

import com.example.CineHive.entity.videotype.NowPlayingMovie;
import com.example.CineHive.entity.videotype.PoPularMovie;
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
@Table(name="nowplaying_movie_genres")
public class NowPlayingMovieGenre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private NowPlayingMovie movie;

}
