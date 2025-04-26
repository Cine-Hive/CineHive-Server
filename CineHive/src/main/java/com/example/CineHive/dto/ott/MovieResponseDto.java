package com.example.CineHive.dto.ott;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {
    private String title;
    private String overview;
    @JsonProperty("poster_path")
    private String posterPath;
    private Double popularity;
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieList {
        private List<MovieResponseDto> results;
    }
}
