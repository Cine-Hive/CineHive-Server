package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

// TMDB Person Details API 응답을 위한 DTO
public record TmdbPersonDetailResponse(
        long id,
        String name,
        String biography,
        LocalDate birthday,
        LocalDate deathday,
        int gender,
        @JsonProperty("profile_path")
        String profilePath,
        BigDecimal popularity,
        @JsonProperty("movie_credits")
        TmdbPersonCreditsResponse movieCredits,
        @JsonProperty("tv_credits")
        TmdbPersonCreditsResponse tvCredits
) {}