package com.example.CineHive.dto.oauth;

import com.example.CineHive.entity.user.ProviderType;

/**
 * 모든 OAuth2 사용자 정보 응답이 구현해야 하는 인터페이스입니다.
 * 각기 다른 공급자의 응답을 표준화된 OAuth2UserInfo로 변환하는 책임을 가집니다.
 */
public interface OAuth2Response {
    /**
     * 공급자별 응답 데이터를 표준 DTO인 OAuth2UserInfo로 변환합니다.
     * @param providerType 소셜 로그인 공급자 타입 (예: KAKAO, NAVER)
     * @return 표준화된 사용자 정보 DTO
     */
    OAuth2UserInfo toUserInfo(ProviderType providerType);
}