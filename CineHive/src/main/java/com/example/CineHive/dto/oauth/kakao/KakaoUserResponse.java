package com.example.CineHive.dto.oauth.kakao;

import com.example.CineHive.dto.oauth.OAuth2Response;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.ProviderType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("connected_at")
        String connectedAt,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) implements OAuth2Response {

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

    public record Profile(
            String nickname,
            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,
            @JsonProperty("profile_image_url")
            String profileImageUrl,
            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {}

    @Override
    public OAuth2UserInfo toUserInfo(ProviderType providerType) {
        return new OAuth2UserInfo(
                this.kakaoAccount().email(),
                this.kakaoAccount().profile().nickname(),
                providerType
        );
    }
}