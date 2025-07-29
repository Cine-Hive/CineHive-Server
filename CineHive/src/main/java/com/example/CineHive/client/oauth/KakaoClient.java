package com.example.CineHive.client.oauth;

import com.example.CineHive.global.config.security.OAuthProperties;
import com.example.CineHive.domain.oauth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.oauth.dto.kakao.KakaoTokenResponse;
import com.example.CineHive.domain.oauth.dto.kakao.KakaoUserResponse;
import com.example.CineHive.domain.oauth.ProviderType;
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
public class KakaoClient implements OAuth2Client {

    private final WebClient webClient;
    private final OAuthProperties.Kakao kakaoProperties;

    public KakaoClient(WebClient webClient, OAuthProperties oAuthProperties) {
        this.webClient = webClient;
        this.kakaoProperties = oAuthProperties.getKakao();
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
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
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoProperties.getClientId());
        formData.add("redirect_uri", kakaoProperties.getRedirectUri());
        formData.add("code", code);
        formData.add("client_secret", kakaoProperties.getClientSecret());

        return webClient.post()
                .uri(kakaoProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .map(KakaoTokenResponse::accessToken);
    }

    private Mono<KakaoUserResponse> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri(kakaoProperties.getUserInfoUri())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponse.class);
    }

    private OAuth2UserInfo toUserInfo(KakaoUserResponse userResponse) {
        log.debug("카카오 사용자 정보: {}", userResponse);
        return userResponse.toUserInfo(getProviderType());
    }
}
