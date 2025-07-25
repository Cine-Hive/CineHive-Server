package com.example.CineHive.service.oauth;

import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.entity.user.ProviderType;

/**
 * 소셜 로그인 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface OAuth2Service {

    /**
     * 각 소셜 플랫폼의 인증 페이지로 리다이렉트할 URL을 생성합니다.
     *
     * @param providerType 소셜 로그인 제공업체
     * @param state CSRF 공격 방어를 위한 고유 상태 값
     * @return 생성된 Redirect URL
     */
    String getRedirectUrl(ProviderType providerType, String state);

    /**
     * [웹 로그인용] 인가 코드를 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     *
     * @param providerType 소셜 로그인 제공업체
     * @param code         제공업체로부터 받은 인가 코드
     * @param state        CSRF 공격 방어를 위해 검증에 사용될 상태 값
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithCode(ProviderType providerType, String code, String state);

    /**
     * [앱 로그인용] 액세스 토큰을 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     *
     * @param providerType 소셜 로그인 제공업체
     * @param accessToken  앱에서 전달받은 액세스 토큰
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken);
}
