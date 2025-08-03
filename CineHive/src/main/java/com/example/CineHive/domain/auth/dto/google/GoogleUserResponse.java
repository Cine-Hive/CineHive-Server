package com.example.CineHive.domain.auth.dto.google;

import com.example.CineHive.domain.auth.dto.OAuth2Response;
import com.example.CineHive.domain.auth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.controller.ProviderType;
import com.fasterxml.jackson.annotation.JsonProperty;

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
) implements OAuth2Response {

        @Override
        public OAuth2UserInfo toUserInfo(ProviderType providerType) {
                return new OAuth2UserInfo(
                        this.email(),
                        this.name(),
                        providerType
                );
        }
}