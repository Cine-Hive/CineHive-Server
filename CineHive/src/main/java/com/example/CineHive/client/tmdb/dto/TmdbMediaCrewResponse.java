package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbMediaCrewResponse(
        long id,
        String name,
        String job,
        String department,
        @JsonProperty("credit_id")
        String creditId
) {}