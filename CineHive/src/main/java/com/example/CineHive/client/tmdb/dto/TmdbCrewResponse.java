package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 개별 제작진(Crew) 정보를 담는 DTO입니다.
 */
public record TmdbCrewResponse(
        Long id,
        String name,
        String job,
        @JsonProperty("profile_path")
        String profilePath
) {}