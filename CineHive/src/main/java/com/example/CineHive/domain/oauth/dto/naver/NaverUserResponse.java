package com.example.CineHive.domain.oauth.dto.naver;

import com.example.CineHive.domain.oauth.dto.OAuth2Response;
import com.example.CineHive.domain.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.oauth.ProviderType;

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