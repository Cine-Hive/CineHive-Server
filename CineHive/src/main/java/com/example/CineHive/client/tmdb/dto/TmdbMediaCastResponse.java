package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMediaCastResponse(
        long id,
        String name,
        String character,
        int order,
        @JsonProperty("credit_id")
        String creditId
) {}