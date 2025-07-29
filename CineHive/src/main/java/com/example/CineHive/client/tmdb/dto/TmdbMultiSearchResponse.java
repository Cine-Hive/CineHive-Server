package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB API의 통합 검색 응답을 담는 DTO입니다.
 * 영화와 TV의 필드명이 달라 헬퍼 메서드로 정보를 통합합니다.
 */
public record TmdbMultiSearchResponse(
        Long id,
        @JsonProperty("media_type")
        String mediaType,
        String title,
        @JsonProperty("original_title")
        String originalTitle,
        @JsonProperty("release_date")
        String releaseDate,
        String name,
        @JsonProperty("original_name")
        String originalName,
        @JsonProperty("first_air_date")
        String firstAirDate,
        String overview,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("backdrop_path")
        String backdropPath,
        @JsonProperty("genre_ids")
        List<Long> genreIds
) {
    /**
     * 영화(title) 또는 TV(name)의 제목을 통합하여 반환합니다.
     * @return 통합된 콘텐츠 제목
     */
    public String getUnifiedTitle() {
        return title != null ? title : name;
    }

    /**
     * 영화(release_date) 또는 TV(first_air_date)의 출시일을 통합하여 반환합니다.
     * @return 통합된 콘텐츠 출시일
     */
    public String getUnifiedReleaseDate() {
        return releaseDate != null ? releaseDate : firstAirDate;
    }
}