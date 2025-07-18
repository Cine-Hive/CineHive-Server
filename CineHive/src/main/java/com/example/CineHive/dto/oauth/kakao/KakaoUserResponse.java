package com.example.CineHive.dto.oauth.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오 사용자 정보 요청에 대한 전체 응답 DTO
 */
public record KakaoUserResponse(
        Long id,

        @JsonProperty("connected_at")
        String connectedAt,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    /**
     * 카카오 계정 정보
     */
    public record KakaoAccount(
            @JsonProperty("has_email")
            Boolean hasEmail,

            @JsonProperty("email_needs_agreement")
            Boolean emailNeedsAgreement,

            @JsonProperty("is_email_valid")
            Boolean isEmailValid,

            @JsonProperty("is_email_verified")
            Boolean isEmailVerified,

            String email,

            Profile profile
    ) {}

    /**
     * 카카오 프로필 정보
     */
    public record Profile(
            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {}
}