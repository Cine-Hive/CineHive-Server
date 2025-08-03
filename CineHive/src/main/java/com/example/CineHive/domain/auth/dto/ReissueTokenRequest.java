package com.example.CineHive.domain.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급 요청")
public record ReissueTokenRequest(
        @NotBlank(message = "리프레시 토큰은 필수입니다.")
        @Schema(description = "클라이언트가 보관하고 있던 JWT 리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJI...")
        String refreshToken
) {}
