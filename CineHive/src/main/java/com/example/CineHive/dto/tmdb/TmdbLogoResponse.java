package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 로고 정보를 담는 DTO입니다.
 */
public record TmdbLogoResponse(
        @JsonProperty("file_path")
        String filePath,
        @JsonProperty("file_type")
        String fileType
) {}