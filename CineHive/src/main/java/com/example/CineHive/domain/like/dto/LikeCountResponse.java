package com.example.CineHive.domain.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "'좋아요' 개수 응답")
public record LikeCountResponse(
        @Schema(description = "'좋아요' 개수")
        int count
) {}