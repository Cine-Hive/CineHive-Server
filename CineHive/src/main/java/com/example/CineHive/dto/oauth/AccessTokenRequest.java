package com.example.CineHive.dto.oauth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "앱 소셜 로그인 시 액세스 토큰을 담는 요청 DTO")
public record AccessTokenRequest(
        @Schema(description = "소셜 로그인 제공업체로부터 발급받은 Access Token")
        String accessToken
) {}