package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbProductionCompany(
        long id,
        @JsonProperty("logo_path") String logoPath,
        String name,
        @JsonProperty("origin_country") String originCountry
) {}