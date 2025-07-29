package com.example.CineHive.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 성공 응답")
public record ReissueTokenResponse(
        @Schema(description = "새로 발급된 JWT 액세스 토큰")
        String accessToken,
        @Schema(description = "새로 발급된 JWT 리프레시 토큰 (보안을 위한 토큰 로테이션)")
        String refreshToken
) {}
