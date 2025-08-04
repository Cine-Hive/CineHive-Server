package com.example.CineHive.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "앱 소셜 로그인 시 액세스 토큰을 담는 요청 DTO")
public record AccessTokenRequest(
        @Schema(description = "소셜 로그인 제공업체로부터 발급받은 Access Token")
        @NotBlank(message = "액세스 토큰은 비어 있을 수 없습니다.")
        String accessToken
) {}