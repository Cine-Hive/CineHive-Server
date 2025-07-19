package com.example.CineHive.client;

import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.dto.oauth.kakao.KakaoTokenResponse;
import com.example.CineHive.dto.oauth.kakao.KakaoUserResponse;
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
public class KakaoClient implements OAuth2Client {

    private final WebClient webClient;

    @Value("${kakao.client.id}")
    private String clientId;
    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
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
        String tokenUri = "https://kauth.kakao.com/oauth/token";
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        return webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .map(KakaoTokenResponse::accessToken);
    }

    private Mono<KakaoUserResponse> fetchUserInfo(String accessToken) {
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";
        return webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponse.class);
    }

    private OAuth2MemberInfo toMemberInfo(KakaoUserResponse userResponse) {
        log.debug("Kakao User Info: {}", userResponse);
        return new OAuth2MemberInfo(
                userResponse.kakaoAccount().email(),
                userResponse.kakaoAccount().profile().nickname(),
                getProviderType()
        );
    }
}