package com.example.CineHive.client;

import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.ProviderType;
import reactor.core.publisher.Mono;

/**
 * 모든 소셜 로그인 클라이언트(네이버, 카카오, 구글 등)가 구현해야 하는 공통 인터페이스입니다.
 * 전략 패턴(Strategy Pattern)의 '전략' 역할을 수행합니다.
 */
public interface OAuth2Client {

    /**
     * 이 클라이언트가 어떤 소셜 로그인 제공업체를 담당하는지 식별합니다.
     * 이 값은 Service 계층에서 적절한 클라이언트를 선택하는 데 사용됩니다.
     *
     * @return 해당 클라이언트의 제공업체 타입 (NAVER, KAKAO, GOOGLE 등)
     */
    ProviderType getProviderType();

    /**
     * [웹 기반 로그인용]
     * 인가 코드를 사용하여 최종 사용자 정보를 조회합니다.
     * 이 메서드는 내부적으로 다음 단계를 수행합니다:
     * 1. 인가 코드로 액세스 토큰 요청
     * 2. 발급받은 액세스 토큰으로 사용자 정보 요청
     * 3. 받은 정보를 표준 {@link OAuth2UserInfo} 객체로 변환
     *
     * @param code 소셜 로그인 제공업체의 리다이렉션을 통해 발급받은 인가 코드
     * @return 비동기적으로 반환될 표준화된 회원 정보 {@code Mono} 객체
     */
    Mono<OAuth2UserInfo> getMemberInfo(String code);

    /**
     * [앱 기반 로그인용]
     * 액세스 토큰을 사용하여 최종 사용자 정보를 조회합니다.
     * 클라이언트 앱(iOS, Android)에서 자체 SDK를 통해 이미 로그인을 완료하고 받은
     * 액세스 토큰을 직접 사용하여 사용자 정보를 요청하는 흐름입니다.
     *
     * @param accessToken 클라이언트 앱에서 직접 전달받은 유효한 액세스 토큰
     * @return 비동기적으로 반환될 표준화된 회원 정보 {@code Mono} 객체
     */
    Mono<OAuth2UserInfo> getMemberInfoByAccessToken(String accessToken);
}