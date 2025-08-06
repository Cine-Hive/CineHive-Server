package com.example.CineHive.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인기 검색어 응답 DTO")
public record TrendingSearchResponse(
        @Schema(description = "순위", example = "1")
        int rank,
        @Schema(description = "검색어 키워드", example = "어벤져스")
        String keyword
) {
}