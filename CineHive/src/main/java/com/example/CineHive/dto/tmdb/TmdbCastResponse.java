package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 개별 출연진(Cast) 정보를 담는 DTO입니다.
 */
public record TmdbCastResponse(
        Long id,
        String name,
        @JsonProperty("profile_path")
        String profilePath,
        String character,
        Integer order
) {}