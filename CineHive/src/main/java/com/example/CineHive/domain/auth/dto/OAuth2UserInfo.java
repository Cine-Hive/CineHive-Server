package com.example.CineHive.domain.auth.controller.dto;

import com.example.CineHive.domain.auth.controller.ProviderType;

/**
 * 다양한 OAuth2 제공업체로부터 받은 사용자 정보를
 * 우리 시스템의 사용자(User)로 처리하기 위한 표준 DTO입니다.
 */
public record OAuth2UserInfo(
        String email,
        String nickname,
        ProviderType providerType
) {}