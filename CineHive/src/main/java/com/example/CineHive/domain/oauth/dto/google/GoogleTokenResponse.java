package com.example.CineHive.domain.oauth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 구글 토큰 요청에 대한 응답 DTO
 */
public record GoogleTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Integer expiresIn,

        String scope,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("id_token")
        String idToken
) {}