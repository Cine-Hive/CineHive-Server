package com.example.CineHive.domain.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필터링 옵션으로 사용될 장르 정보")
public record GenreOption(
        @Schema(description = "장르 고유 ID")
        Long id,
        @Schema(description = "장르 이름")
        String name
) {}