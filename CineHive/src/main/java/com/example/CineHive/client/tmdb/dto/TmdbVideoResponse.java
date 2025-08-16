package com.example.CineHive.client.tmdb.dto;

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
        String publishedAt,
        @JsonProperty("iso_639_1")
        String iso6391,
        @JsonProperty("iso_3166_1")
        String iso31661,
        Integer size
) {}