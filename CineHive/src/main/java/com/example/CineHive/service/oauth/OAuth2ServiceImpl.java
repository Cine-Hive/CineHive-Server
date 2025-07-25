package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.config.OAuthProperties;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final List<OAuth2Client> clients;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuthProperties oAuthProperties;

    private Map<ProviderType, OAuth2Client> clientMap;

    @PostConstruct
    public void init() {
        clientMap = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuth2Client::getProviderType, Function.identity())
        );
    }

    @Override
    public String getRedirectUrl(ProviderType providerType, String state) {
        return switch (providerType) {
            case NAVER -> buildNaverRedirectUrl(state);
            case KAKAO -> buildKakaoRedirectUrl();
            case GOOGLE -> buildGoogleRedirectUrl(state);
            case LOCAL -> throw new BusinessException("지원하지 않는 플랫폼입니다: " + providerType, ErrorCode.INVALID_INPUT_VALUE);
        };
    }

    /**
     * 여기를 수정해야 합니다.
     * 인터페이스에 맞게 3개의 파라미터(providerType, code, state)를 받도록 수정합니다.
     */
    @Override
    @Transactional
    public LoginResponse loginWithCode(ProviderType providerType, String code, String state) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo userInfo = client.getUserInfo(code, state)
                .onErrorMap(error -> {
                    log.error("OAuth 통신 오류 (인가 코드 사용): {}", error.getMessage());
                    return new BusinessException(ErrorCode.OAUTH_COMMUNICATION_ERROR);
                })
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN)))
                .block();
        return processLogin(userInfo);
    }

    @Override
    @Transactional
    public LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo userInfo = client.getUserInfoByAccessToken(accessToken)
                .onErrorMap(error -> {
                    log.error("OAuth 통신 오류 (액세스 토큰 사용): {}", error.getMessage());
                    return new BusinessException(ErrorCode.OAUTH_COMMUNICATION_ERROR);
                })
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN)))
                .block();
        return processLogin(userInfo);
    }

    private LoginResponse processLogin(OAuth2UserInfo userInfo) {
        if (userInfo == null || userInfo.email() == null) {
            throw new BusinessException("소셜 로그인 정보 처리 중 오류가 발생했습니다 (이메일 정보 없음).", ErrorCode.OAUTH_COMMUNICATION_ERROR);
        }
        boolean isNewUser = !userRepository.existsByEmail(userInfo.email());
        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> registerNewUser(userInfo));
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponse(token, isNewUser, LoginResponse.UserInfo.from(user));
    }

    private User registerNewUser(OAuth2UserInfo userInfo) {
        log.info("신규 소셜 사용자 가입: {}", userInfo.email());
        String nickname = resolveUniqueNickname(userInfo.nickname(), userInfo.providerType());
        User newUser = User.builder()
                .email(userInfo.email())
                .password("OAUTH_USER_NO_PASSWORD")
                .name(userInfo.nickname())
                .nickname(nickname)
                .gender(Gender.OTHER)
                .genres(Collections.emptySet())
                .provider(userInfo.providerType())
                .build();
        return userRepository.save(newUser);
    }

    private String resolveUniqueNickname(String nickname, ProviderType providerType) {
        String resolvedNickname = nickname;
        int suffix = 1;
        while (userRepository.existsByNickname(resolvedNickname)) {
            resolvedNickname = String.format("%s (%s %d)", nickname, providerType.name(), suffix++);
        }
        return resolvedNickname;
    }

    private OAuth2Client getClient(ProviderType providerType) {
        OAuth2Client client = clientMap.get(providerType);
        if (client == null) {
            throw new BusinessException("지원하지 않는 소셜 로그인입니다: " + providerType, ErrorCode.INVALID_INPUT_VALUE);
        }
        return client;
    }

    private String buildNaverRedirectUrl(String state) {
        OAuthProperties.Naver naver = oAuthProperties.getNaver();
        return UriComponentsBuilder.fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", naver.getClientId())
                .queryParam("redirect_uri", naver.getRedirectUri())
                .queryParam("state", state)
                .build().toUriString();
    }

    private String buildKakaoRedirectUrl() {
        OAuthProperties.Kakao kakao = oAuthProperties.getKakao();
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", kakao.getClientId())
                .queryParam("redirect_uri", kakao.getRedirectUri())
                .build().toUriString();
    }

    private String buildGoogleRedirectUrl(String state) {
        OAuthProperties.Google google = oAuthProperties.getGoogle();
        return UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", google.getClientId())
                .queryParam("redirect_uri", google.getRedirectUri())
                .queryParam("scope", google.getScope())
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .build().toUriString();
    }
}