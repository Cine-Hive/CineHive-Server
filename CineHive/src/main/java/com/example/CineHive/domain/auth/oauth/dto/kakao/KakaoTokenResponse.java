package com.example.CineHive.domain.auth.oauth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 토큰 요청에 대한 응답 DTO
 */
public record KakaoTokenResponse(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("refresh_token_expires_in")
        Integer refreshTokenExpiresIn,

        String scope
) {}