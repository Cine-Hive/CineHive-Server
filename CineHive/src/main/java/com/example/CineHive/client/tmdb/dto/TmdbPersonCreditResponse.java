package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.CineHive.domain.media.enums.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TmdbPersonCreditResponse(
        long id,
        String title, // 영화용
        String name,  // TV용
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("media_type") MediaType mediaType,
        @JsonProperty("release_date") LocalDate releaseDate, // 영화용
        @JsonProperty("first_air_date") LocalDate firstAirDate, // TV용
        @JsonProperty("vote_average") BigDecimal voteAverage,
        @JsonProperty("vote_count") int voteCount,
        String character,
        @JsonProperty("credit_id") String creditId,
        int order,
        String department,
        String job
) {}