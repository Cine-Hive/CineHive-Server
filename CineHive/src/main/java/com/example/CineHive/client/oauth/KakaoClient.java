package com.example.CineHive.client.oauth;

import com.example.CineHive.global.properties.OAuthProperties;
import com.example.CineHive.domain.auth.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.oauth.dto.kakao.KakaoTokenResponse;
import com.example.CineHive.domain.auth.oauth.dto.kakao.KakaoUserResponse;
import com.example.CineHive.domain.auth.enums.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class KakaoClient implements OAuth2Client {

    private final RestTemplate restTemplate;
    private final OAuthProperties.Kakao kakaoProperties;

    public KakaoClient(RestTemplateBuilder restTemplateBuilder, OAuthProperties oAuthProperties) {
        this.restTemplate = restTemplateBuilder.build();
        this.kakaoProperties = oAuthProperties.getKakao();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
    }

    @Override
    public OAuth2UserInfo getUserInfo(String code, String state) {
        String accessToken = getAccessToken(code);
        KakaoUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    @Override
    public OAuth2UserInfo getUserInfoByAccessToken(String accessToken) {
        KakaoUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoProperties.getClientId());
        formData.add("redirect_uri", kakaoProperties.getRedirectUri());
        formData.add("code", code);
        formData.add("client_secret", kakaoProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        KakaoTokenResponse tokenResponse = restTemplate.postForObject(
                kakaoProperties.getTokenUri(),
                requestEntity,
                KakaoTokenResponse.class
        );

        if (tokenResponse == null) {
            throw new IllegalStateException("카카오 토큰 응답을 받지 못했습니다.");
        }
        return tokenResponse.accessToken();
    }

    private KakaoUserResponse fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserResponse> responseEntity = restTemplate.exchange(
                kakaoProperties.getUserInfoUri(),
                HttpMethod.GET,
                requestEntity,
                KakaoUserResponse.class
        );

        return responseEntity.getBody();
    }

    private OAuth2UserInfo toUserInfo(KakaoUserResponse userResponse) {
        log.debug("카카오 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}