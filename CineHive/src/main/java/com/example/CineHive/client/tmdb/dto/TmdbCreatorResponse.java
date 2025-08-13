package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCreatorResponse(
        long id,
        String name,
        @JsonProperty("profile_path") String profilePath
) {}
