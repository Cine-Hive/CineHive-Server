package com.example.CineHive.dto.video.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private List<MovieDTO> results;

    public static class MovieDTO {
        private long id;
        private String title;
        private String overview;
        private String posterPath;
        private String backdropPath;
        private String releaseDate;
        private List<Integer> genreIds;
        private double voteAverage;
        private int voteCount;
        private double popularity;
        private boolean adult;
    }
}
