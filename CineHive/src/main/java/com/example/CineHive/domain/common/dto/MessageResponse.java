package com.example.CineHive.domain.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 단순 성공 메시지 응답을 위한 DTO입니다.
 */
@Schema(description = "성공 메시지 응답")
public record MessageResponse(
        @Schema(description = "응답 메시지")
        String message
) {}