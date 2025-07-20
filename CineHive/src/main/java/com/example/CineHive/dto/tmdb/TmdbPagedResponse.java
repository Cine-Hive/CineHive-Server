package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB API의 페이징 처리된 목록 응답을 위한 제네릭 DTO입니다.
 * @param <T> 목록에 포함될 콘텐츠의 타입
 */
public record TmdbPagedResponse<T>(
        int page,
        List<T> results,
        @JsonProperty("total_pages")
        int totalPages,
        @JsonProperty("total_results")
        int totalResults
) {}