package com.example.CineHive.dto.oauth.google;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 구글 사용자 정보 요청에 대한 응답 DTO
 */
public record GoogleUserResponse(
        String id,
        String email,
        @JsonProperty("verified_email")
        Boolean verifiedEmail,
        String name,
        @JsonProperty("given_name")
        String givenName,
        @JsonProperty("family_name")
        String familyName,
        String picture,
        String locale
) {}