package com.example.CineHive.domain.auth.dto.naver;

import com.example.CineHive.domain.auth.dto.OAuth2Response;
import com.example.CineHive.domain.auth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.ProviderType;

public record NaverUserResponse(
        String resultcode,
        String message,
        NaverUser response
) implements OAuth2Response {

    @Override
    public OAuth2UserInfo toUserInfo(ProviderType providerType) {
        return new OAuth2UserInfo(
                this.response().email(),
                this.response().nickname(),
                providerType
        );
    }
}