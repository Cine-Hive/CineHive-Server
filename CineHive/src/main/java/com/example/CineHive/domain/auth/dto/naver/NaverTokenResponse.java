package com.example.CineHive.domain.auth.dto.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 네이버 토큰 요청에 대한 응답 DTO
 */
public record NaverTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Integer expiresIn
) {}