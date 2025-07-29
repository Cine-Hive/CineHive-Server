package com.example.CineHive.client.oauth;

import com.example.CineHive.global.config.security.OAuthProperties;
import com.example.CineHive.domain.auth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.auth.dto.google.GoogleTokenResponse;
import com.example.CineHive.domain.auth.dto.google.GoogleUserResponse;
import com.example.CineHive.domain.auth.ProviderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GoogleClient implements OAuth2Client {

    private final WebClient webClient;
    private final OAuthProperties.Google googleProperties;

    public GoogleClient(WebClient webClient, OAuthProperties oAuthProperties) {
        this.webClient = webClient;
        this.googleProperties = oAuthProperties.getGoogle();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }

    @Override
    public Mono<OAuth2UserInfo> getUserInfo(String code, String state) {
        return getAccessToken(code)
                .flatMap(this::fetchUserInfo)
                .map(this::toUserInfo);
    }

    @Override
    public Mono<OAuth2UserInfo> getUserInfoByAccessToken(String accessToken) {
        return fetchUserInfo(accessToken)
                .map(this::toUserInfo);
    }

    private Mono<String> getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("redirect_uri", googleProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");

        return webClient.post()
                .uri(googleProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .map(GoogleTokenResponse::accessToken);
    }

    private Mono<GoogleUserResponse> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri(googleProperties.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserResponse.class);
    }

    private OAuth2UserInfo toUserInfo(GoogleUserResponse userResponse) {
        log.debug("구글 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}
