package com.example.CineHive.dto.oauth;

import com.example.CineHive.entity.member.ProviderType;

/**
 * 다양한 OAuth2 제공업체로부터 받은 사용자 정보를
 * 우리 시스템의 회원(Member)으로 처리하기 위한 표준 DTO.
 */
public record OAuth2MemberInfo(
        String email,
        String nickname,
        ProviderType providerType
) {}