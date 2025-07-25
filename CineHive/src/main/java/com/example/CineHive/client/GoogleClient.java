package com.example.CineHive.client;

import com.example.CineHive.config.OAuthProperties;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.dto.oauth.google.GoogleTokenResponse;
import com.example.CineHive.dto.oauth.google.GoogleUserResponse;
import com.example.CineHive.entity.user.ProviderType;
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
        // 구글은 토큰 요청 시 state를 사용하지 않으므로 무시
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
        String tokenUri = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("redirect_uri", googleProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .map(GoogleTokenResponse::accessToken);
    }

    private Mono<GoogleUserResponse> fetchUserInfo(String accessToken) {
        String userInfoUri = "https://www.googleapis.com/oauth2/v2/userinfo";
        return webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserResponse.class);
    }

    private OAuth2UserInfo toUserInfo(GoogleUserResponse userResponse) {
        log.debug("구글 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}
