package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "TMDB API의 인물 상세 정보 응답 DTO")
public record TmdbPersonDetailResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("original_name")
        String originalName,

        @JsonProperty("biography")
        String biography,

        @JsonProperty("profile_path")
        String profilePath,

        @JsonProperty("birthday")
        LocalDate birthday,

        @JsonProperty("deathday")
        LocalDate deathday,

        @JsonProperty("known_for_department")
        String knownForDepartment,

        /**
         * TMDB API 기준 성별 코드.
         * 0: Not set, 1: Female, 2: Male, 3: Non-binary
         */
        @JsonProperty("gender")
        int gender,

        @JsonProperty("homepage")
        String homepage,

        @JsonProperty("movie_credits")
        TmdbPersonCreditsResponse movieCredits,

        @JsonProperty("tv_credits")
        TmdbPersonCreditsResponse tvCredits
) {
}