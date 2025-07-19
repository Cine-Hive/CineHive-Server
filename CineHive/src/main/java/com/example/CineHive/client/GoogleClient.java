package com.example.CineHive.client;

import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.dto.oauth.google.GoogleTokenResponse;
import com.example.CineHive.dto.oauth.google.GoogleUserResponse;
import com.example.CineHive.entity.user.ProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleClient implements OAuth2Client {

    private final WebClient webClient;

    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }

    @Override
    public Mono<OAuth2MemberInfo> getMemberInfo(String code) {
        return getAccessToken(code)
                .flatMap(this::fetchUserInfo)
                .map(this::toMemberInfo);
    }

    @Override
    public Mono<OAuth2MemberInfo> getMemberInfoByAccessToken(String accessToken) {
        return fetchUserInfo(accessToken)
                .map(this::toMemberInfo);
    }

    private Mono<String> getAccessToken(String code) {
        String tokenUri = "https://oauth2.googleapis.com/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
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

    private OAuth2MemberInfo toMemberInfo(GoogleUserResponse userResponse) {
        log.debug("Google User Info: {}", userResponse);
        return new OAuth2MemberInfo(
                userResponse.email(),
                userResponse.name(),
                getProviderType()
        );
    }
}