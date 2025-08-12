package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbReleaseDate(
        String certification,
        @JsonProperty("release_date") String releaseDate,
        int type // 1: Premiere, 2: Theatrical (limited), 3: Theatrical, ...
) {}
