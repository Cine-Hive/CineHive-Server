<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/oauth/service/OAuth2Service.java
package com.example.CineHive.domain.oauth.service;

import com.example.CineHive.domain.auth.entity.ProviderType;
=======
package com.example.CineHive.domain.oauth;

import com.example.CineHive.domain.auth.ProviderType;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/oauth/OAuth2Service.java
import com.example.CineHive.domain.auth.dto.LoginResponse;

/**
 * 소셜 로그인(OAuth2) 흐름을 총괄하는 서비스 인터페이스입니다.
 * 각 소셜 플랫폼별 인증 URL 생성, 인가 코드를 이용한 로그인, 액세스 토큰을 이용한 로그인 기능을 정의합니다.
 */
public interface OAuth2Service {

    /**
     * 각 소셜 플랫폼의 인증 페이지로 리다이렉트할 URL을 생성합니다.
     *
     * @param providerType 소셜 로그인 제공업체 (NAVER, KAKAO, GOOGLE)
     * @param state        CSRF 공격 방어를 위한 고유 상태 값
     * @return 생성된 Redirect URL
     */
    String getRedirectUrl(ProviderType providerType, String state);

    /**
     * [웹 로그인용] 인가 코드를 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     * CSRF 방어를 위한 state 값 검증을 포함하며, 성공 시 로그인 이력을 기록합니다.
     *
     * @param providerType   소셜 로그인 제공업체
     * @param code           제공업체로부터 받은 인가 코드
     * @param receivedState  플랫폼 리다이렉션 시 전달받은 state 값
     * @param sessionState   CSRF 방어를 위해 세션에 저장했던 state 값
     * @param userAgent      로그인 이력 기록을 위한 사용자의 User-Agent 정보
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithCode(ProviderType providerType, String code, String receivedState, String sessionState, String userAgent);

    /**
     * [앱 로그인용] 액세스 토큰을 사용하여 로그인/회원가입을 처리하고 JWT를 발급합니다.
     * 성공 시 로그인 이력을 기록합니다.
     *
     * @param providerType 소셜 로그인 제공업체
     * @param accessToken  클라이언트(앱)에서 직접 전달받은 유효한 액세스 토큰
     * @param userAgent    로그인 이력 기록을 위한 사용자의 User-Agent 정보
     * @return 로그인 성공 시 JWT 토큰과 회원 정보를 담은 DTO
     */
    LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken, String userAgent);
}