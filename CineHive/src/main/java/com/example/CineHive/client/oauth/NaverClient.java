package com.example.CineHive.client.oauth;

import com.example.CineHive.global.config.security.OAuthProperties;
import com.example.CineHive.domain.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.oauth.dto.naver.NaverTokenResponse;
import com.example.CineHive.domain.oauth.dto.naver.NaverUserResponse;
import com.example.CineHive.domain.oauth.ProviderType;
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
        return getAccessToken(code, state)
                .flatMap(this::fetchUserInfo)
                .map(this::toUserInfo);
    }

    @Override
    public Mono<OAuth2UserInfo> getUserInfoByAccessToken(String accessToken) {
        return fetchUserInfo(accessToken)
                .map(this::toUserInfo);
    }

    private Mono<String> getAccessToken(String code, String state) {
        return webClient.post()
                .uri(naverProperties.getTokenUri(), uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", naverProperties.getClientId())
                        .queryParam("client_secret", naverProperties.getClientSecret())
                        .queryParam("code", code)
                        .queryParam("state", state)
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .bodyToMono(NaverTokenResponse.class)
                .map(NaverTokenResponse::accessToken);
    }

    private Mono<NaverUserResponse> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri(naverProperties.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(NaverUserResponse.class);
    }

    private OAuth2UserInfo toUserInfo(NaverUserResponse userResponse) {
        log.debug("네이버 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}
