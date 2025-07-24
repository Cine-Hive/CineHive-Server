package com.example.CineHive.dto.oauth.naver;

import com.example.CineHive.dto.oauth.OAuth2Response;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.ProviderType;

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