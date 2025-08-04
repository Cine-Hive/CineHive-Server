package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "TMDB API의 인물 목록 조회 시, 각 인물 항목에 대한 응답 DTO")
public record TmdbPersonInListResponse(
        @JsonProperty("id")
        Long id,
        @JsonProperty("name")
        String name,
        @JsonProperty("profile_path")
        String profilePath,
        @JsonProperty("popularity")
        double popularity,
        @JsonProperty("known_for")
        List<TmdbKnownForResponse> knownFor
) {
}