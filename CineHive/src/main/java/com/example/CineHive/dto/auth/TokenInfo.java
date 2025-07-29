package com.example.CineHive.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 재발급 관련 요청 및 응답을 위한 클래스들을 포함합니다.
 */
public class TokenInfo {

    /**
     * 토큰 재발급 요청 시 클라이언트로부터 Refresh Token을 받기 위한 DTO입니다.
     */
    @Schema(description = "토큰 재발급 요청")
    public record ReissueRequest(
            @NotBlank(message = "리프레시 토큰은 필수입니다.")
            @Schema(description = "클라이언트가 보관하고 있던 JWT 리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJI...")
            String refreshToken
    ) {}

    /**
     * 토큰 재발급 성공 시 클라이언트에게 새로운 토큰들을 전달하기 위한 DTO입니다.
     */
    @Schema(description = "토큰 재발급 성공 응답")
    public record ReissueResponse(
            @Schema(description = "새로 발급된 JWT 액세스 토큰")
            String accessToken,
            @Schema(description = "새로 발급된 JWT 리프레시 토큰 (보안을 위한 토큰 로테이션)")
            String refreshToken
    ) {}
}
