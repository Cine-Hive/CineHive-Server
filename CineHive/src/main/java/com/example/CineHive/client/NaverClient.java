package com.example.CineHive.client;

import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.dto.oauth.naver.NaverTokenResponse;
import com.example.CineHive.dto.oauth.naver.NaverUserResponse;
import com.example.CineHive.entity.user.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverClient implements OAuth2Client {

    private final WebClient webClient;

    @Value("${naver.client.id}")
    private String clientId;
    @Value("${naver.client.secret}")
    private String clientSecret;
    @Value("${naver.redirect.uri}")
    private String redirectUri;

    @Override
    public ProviderType getProviderType() {
        return ProviderType.NAVER;
    }

    @Override
    public Mono<OAuth2UserInfo> getMemberInfo(String code) {
        return getAccessToken(code)
                .flatMap(this::fetchUserInfo)
                .map(this::toMemberInfo);
    }

    @Override
    public Mono<OAuth2UserInfo> getMemberInfoByAccessToken(String accessToken) {
        return fetchUserInfo(accessToken)
                .map(this::toMemberInfo);
    }

    private Mono<String> getAccessToken(String code) {
        String tokenUri = "https://nid.naver.com/oauth2.0/token";
        return webClient.post()
                .uri(tokenUri, uriBuilder -> uriBuilder
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("code", code)
                        .queryParam("state", "STATE_STRING") // CSRF 방지를 위한 상태 토큰
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

    private OAuth2UserInfo toMemberInfo(NaverUserResponse userResponse) {
        log.debug("Naver User Info: {}", userResponse);
        return new OAuth2UserInfo(
                userResponse.response().email(),
                userResponse.response().nickname(),
                getProviderType()
        );
    }
}