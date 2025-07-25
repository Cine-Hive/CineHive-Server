package com.example.CineHive.client;

import com.example.CineHive.config.OAuthProperties;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.dto.oauth.naver.NaverTokenResponse;
import com.example.CineHive.dto.oauth.naver.NaverUserResponse;
import com.example.CineHive.entity.user.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NaverClient implements OAuth2Client {

    private final WebClient webClient;
    private final OAuthProperties.Naver naverProperties;

    // 생성자를 통해 WebClient와 Naver 설정 정보 주입
    public NaverClient(WebClient webClient, OAuthProperties oAuthProperties) {
        this.webClient = webClient;
        this.naverProperties = oAuthProperties.getNaver();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.NAVER;
    }

    @Override
    public Mono<OAuth2UserInfo> getUserInfo(String code, String state) {
        return getAccessToken(code, state) // state 전달
                .flatMap(this::fetchUserInfo)
                .map(this::toUserInfo);
    }

    @Override
    public Mono<OAuth2UserInfo> getUserInfoByAccessToken(String accessToken) {
        return fetchUserInfo(accessToken)
                .map(this::toUserInfo);
    }

    private Mono<String> getAccessToken(String code, String state) {
        String tokenUri = "https://nid.naver.com/oauth2.0/token";
        return webClient.post()
                .uri(tokenUri, uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", naverProperties.getClientId())
                        .queryParam("client_secret", naverProperties.getClientSecret())
                        .queryParam("code", code)
                        .queryParam("state", state) // Controller에서 전달받은 state 사용
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .map(NaverTokenResponse::accessToken);
    }

    private Mono<NaverUserResponse> fetchUserInfo(String accessToken) {
        String userInfoUri = "https://openapi.naver.com/v1/nid/me";
        return webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserResponse.class);
    }

    private OAuth2UserInfo toUserInfo(NaverUserResponse userResponse) {
        log.debug("네이버 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}
