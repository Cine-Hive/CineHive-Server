package com.example.CineHive.client.oauth;

import com.example.CineHive.domain.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.oauth.ProviderType;
import reactor.core.publisher.Mono;

public interface OAuth2Client {

    ProviderType getProviderType();

    /**
     * [웹 로그인용] 인가 코드를 사용하여 사용자 정보를 조회합니다.
     * @param code 소셜 로그인 제공업체로부터 발급받은 인가 코드
     * @param state CSRF 방어용으로 전달된 상태 값 (Naver 등 일부 플랫폼에서 토큰 요청 시 필요)
     * @return 비동기적으로 반환될 표준화된 사용자 정보 Mono 객체
     */
    Mono<OAuth2UserInfo> getUserInfo(String code, String state);

    /**
     * [앱 로그인용] 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     * @param accessToken 클라이언트 앱에서 직접 전달받은 유효한 액세스 토큰
     * @return 비동기적으로 반환될 표준화된 사용자 정보 Mono 객체
     */
    Mono<OAuth2UserInfo> getUserInfoByAccessToken(String accessToken);
}
