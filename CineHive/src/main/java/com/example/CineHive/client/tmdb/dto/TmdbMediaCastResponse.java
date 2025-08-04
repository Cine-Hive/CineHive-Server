package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMediaCastResponse(
        @JsonProperty("id")
        Long id,
        @JsonProperty("name")
        String name,
        @JsonProperty("character")
        String character,
        @JsonProperty("profile_path")
        String profilePath
) {
}