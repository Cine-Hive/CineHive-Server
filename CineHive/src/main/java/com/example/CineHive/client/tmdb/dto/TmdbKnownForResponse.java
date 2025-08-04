package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TMDB API의 인물 목록 조회 시, 'known_for' 항목의 상세 정보")
public record TmdbKnownForResponse(
        @JsonProperty("id")
        Long id,
        @JsonProperty("media_type")
        String mediaType,
        @JsonProperty("title")
        String title,
        @JsonProperty("name")
        String name
) {
    /**
     * mediaType에 따라 영화 제목 또는 TV 시리즈 제목을 반환합니다.
     * @return 미디어의 제목
     */
    public String getTitle() {
        return "movie".equalsIgnoreCase(mediaType) ? title : name;
    }
}