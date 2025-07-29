package com.example.CineHive.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "선호 장르 변경 요청 DTO")
public record UpdateGenresRequest(
        @Schema(description = "새로운 선호 장르 목록", example = "[\"ACTION\", \"DRAMA\"]")
        List<String> genres
) {}