package com.example.CineHive.domain.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "정렬 옵션 정보")
public record SortOption(
        @Schema(description = "API에서 사용하는 정렬 값", example = "popularity.desc")
        String value,
        @Schema(description = "UI에 표시될 정렬 이름", example = "인기순")
        String label
) {}