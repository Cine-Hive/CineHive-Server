package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 개별 이미지 정보를 담는 DTO입니다.
 */
public record TmdbImageResponse(
        @JsonProperty("aspect_ratio")
        Double aspectRatio,
        Integer height,
        @JsonProperty("iso_639_1")
        String iso_639_1,
        @JsonProperty("file_path")
        String filePath,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Integer width
) {}