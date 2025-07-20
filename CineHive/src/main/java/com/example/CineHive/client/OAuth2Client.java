package com.example.CineHive.client;

import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.ProviderType;
import reactor.core.publisher.Mono;

/**
 * 모든 소셜 로그인 클라이언트가 구현해야 하는 공통 인터페이스입니다.
 * 전략 패턴(Strategy Pattern)의 '전략' 역할을 수행합니다.
 */
public interface OAuth2Client {

    /**
     * 이 클라이언트가 담당하는 소셜 로그인 제공업체를 식별합니다.
     * @return 해당 클라이언트의 제공업체 타입 (NAVER, KAKAO, GOOGLE 등)
     */
    ProviderType getProviderType();

    /**
     * [웹 로그인용] 인가 코드를 사용하여 사용자 정보를 조회합니다.
     * @param code 소셜 로그인 제공업체로부터 발급받은 인가 코드
     * @return 비동기적으로 반환될 표준화된 사용자 정보 Mono 객체
     */
    Mono<OAuth2UserInfo> getUserInfo(String code);

    /**
     * [앱 로그인용] 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     * @param accessToken 클라이언트 앱에서 직접 전달받은 유효한 액세스 토큰
     * @return 비동기적으로 반환될 표준화된 사용자 정보 Mono 객체
     */
    Mono<OAuth2UserInfo> getUserInfoByAccessToken(String accessToken);
}