package com.example.CineHive.client.oauth;

import com.example.CineHive.domain.auth.ProviderType;
import com.example.CineHive.domain.auth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.dto.google.GoogleTokenResponse;
import com.example.CineHive.domain.auth.dto.google.GoogleUserResponse;
import com.example.CineHive.global.config.security.OAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GoogleClient implements OAuth2Client {

    private final RestTemplate restTemplate;
    private final OAuthProperties.Google googleProperties;

    public GoogleClient(RestTemplateBuilder restTemplateBuilder, OAuthProperties oAuthProperties) {
        this.restTemplate = restTemplateBuilder.build();
        this.googleProperties = oAuthProperties.getGoogle();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }

    @Override
    public OAuth2UserInfo getUserInfo(String code, String state) {
        String accessToken = getAccessToken(code);
        GoogleUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    @Override
    public OAuth2UserInfo getUserInfoByAccessToken(String accessToken) {
        GoogleUserResponse userResponse = fetchUserInfo(accessToken);
        return toUserInfo(userResponse);
    }

    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("redirect_uri", googleProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        ResponseEntity<GoogleTokenResponse> responseEntity = restTemplate.postForEntity(
                googleProperties.getTokenUri(),
                requestEntity,
                GoogleTokenResponse.class
        );

        GoogleTokenResponse tokenResponse = responseEntity.getBody();
        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new IllegalStateException("구글 토큰 응답에서 액세스 토큰을 찾을 수 없습니다.");
        }
        return tokenResponse.accessToken();
    }

    private GoogleUserResponse fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserResponse> responseEntity = restTemplate.exchange(
                googleProperties.getUserInfoUri(),
                HttpMethod.GET,
                requestEntity,
                GoogleUserResponse.class
        );
        return responseEntity.getBody();
    }

    private OAuth2UserInfo toUserInfo(GoogleUserResponse userResponse) {
        log.debug("구글 사용자 정보: {}", userResponse);
        if (userResponse == null) {
            throw new IllegalStateException("구글 사용자 정보 응답이 null입니다.");
        }
        return userResponse.toUserInfo(getProviderType());
    }
}