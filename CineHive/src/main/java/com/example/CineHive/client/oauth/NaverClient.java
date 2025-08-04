package com.example.CineHive.client.oauth;

import com.example.CineHive.global.properties.OAuthProperties;
import com.example.CineHive.domain.auth.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.oauth.dto.naver.NaverTokenResponse;
import com.example.CineHive.domain.auth.oauth.dto.naver.NaverUserResponse;
import com.example.CineHive.domain.auth.enums.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class NaverClient implements OAuth2Client {

    private final RestTemplate restTemplate;
    private final OAuthProperties.Naver naverProperties;

    public NaverClient(RestTemplateBuilder restTemplateBuilder, OAuthProperties oAuthProperties) {
        this.restTemplate = restTemplateBuilder.build();
        this.naverProperties = oAuthProperties.getNaver();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.NAVER;
    }

    @Override
    public OAuth2UserInfo getUserInfo(String code, String state) {
        String accessToken = getAccessToken(code, state);
        NaverUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    @Override
    public OAuth2UserInfo getUserInfoByAccessToken(String accessToken) {
        NaverUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    private String getAccessToken(String code, String state) {
        String uri = UriComponentsBuilder
                .fromUriString(naverProperties.getTokenUri())
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", naverProperties.getClientId())
                .queryParam("client_secret", naverProperties.getClientSecret())
                .queryParam("code", code)
                .queryParam("state", state)
                .toUriString();

        NaverTokenResponse tokenResponse = restTemplate.postForObject(uri, null, NaverTokenResponse.class);

        if (tokenResponse == null) {
            // 이 예외는 ServiceImpl에서 RestClientException으로 잡힙니다.
            throw new IllegalStateException("네이버 토큰 응답을 받지 못했습니다.");
        }
        return tokenResponse.accessToken();
    }

    private NaverUserResponse fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<NaverUserResponse> responseEntity = restTemplate.exchange(
                naverProperties.getUserInfoUri(),
                HttpMethod.GET,
                requestEntity,
                NaverUserResponse.class
        );

        return responseEntity.getBody();
    }

    private OAuth2UserInfo toUserInfo(NaverUserResponse userResponse) {
        log.debug("네이버 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}