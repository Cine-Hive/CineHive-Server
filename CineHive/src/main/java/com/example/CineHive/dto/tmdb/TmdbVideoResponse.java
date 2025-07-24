package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 개별 비디오 정보를 담는 DTO입니다.
 */
public record TmdbVideoResponse(
        String name,
        String key, // YouTube video key
        String site,
        String type, // e.g., "Trailer", "Teaser"
        boolean official,
        @JsonProperty("published_at")
        String publishedAt
) {}