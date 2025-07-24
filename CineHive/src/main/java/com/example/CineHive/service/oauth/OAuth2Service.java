package com.example.CineHive.service.oauth;

import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.entity.user.ProviderType;

/**
 * 소셜 로그인 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface OAuth2Service {

    /**
     * [웹 로그인용] 인가 코드를 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     *
     * @param providerType 소셜 로그인 제공업체 (NAVER, KAKAO, GOOGLE)
     * @param code         제공업체로부터 받은 인가 코드
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithCode(ProviderType providerType, String code);

    /**
     * [앱 로그인용] 액세스 토큰을 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     *
     * @param providerType 소셜 로그인 제공업체 (NAVER, KAKAO, GOOGLE)
     * @param accessToken  앱에서 전달받은 액세스 토큰
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken);

}