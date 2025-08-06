package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbChangeItemResponse(
        @JsonProperty("id")
        Long id,
        @JsonProperty("adult")
        boolean adult
) {}