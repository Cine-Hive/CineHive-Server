package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record TmdbPersonCreditResponse(
        @JsonProperty("id")
        Long id,
        @JsonProperty("title")
        String title,
        @JsonProperty("name")
        String name,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("media_type")
        String mediaType,
        @JsonProperty("character")
        String character,
        @JsonProperty("job")
        String job,
        @JsonProperty("release_date")
        LocalDate releaseDate,
        @JsonProperty("first_air_date")
        LocalDate firstAirDate
) {
    public String getTitle() {
        return "movie".equals(mediaType) ? title : name;
    }

    public LocalDate getReleaseDate() {
        return "movie".equals(mediaType) ? releaseDate : firstAirDate;
    }
}