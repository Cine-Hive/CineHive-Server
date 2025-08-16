package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 Network 정보를 담는 DTO입니다.
 */
public record TmdbNetworkResponse(
        Long id,
        String name,
        @JsonProperty("logo_path") String logoPath,
        @JsonProperty("origin_country") String originCountry
) {}