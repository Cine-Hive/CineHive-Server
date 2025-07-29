package com.example.CineHive.domain.dislike.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "'싫어요' 개수 응답")
public record DislikeCountResponse(
        @Schema(description = "'싫어요' 개수")
        int count
) {}