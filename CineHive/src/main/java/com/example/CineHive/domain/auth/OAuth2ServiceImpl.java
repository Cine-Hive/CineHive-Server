package com.example.CineHive.domain.auth;

import com.example.CineHive.client.oauth.OAuth2Client;
import com.example.CineHive.domain.auth.dto.LoginResponse;
import com.example.CineHive.domain.auth.dto.OAuth2UserInfo;
import com.example.CineHive.domain.user.Gender;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRole;
import com.example.CineHive.global.config.security.OAuthProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthProperties oAuthProperties;

    @Value("${app.jwt.refresh-token-expiration}")
    private String refreshTokenExpirationIso;

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

    @Override
    @Transactional
    public LoginResponse loginWithCode(ProviderType providerType, String code, String receivedState, String sessionState, String userAgent) {
        if (providerType.isStateRequired()) {
            if (sessionState == null || !sessionState.equals(receivedState)) {
                log.warn("OAuth State 값이 일치하지 않습니다. CSRF 공격일 수 있습니다. Session State: {}, Received State: {}", sessionState, receivedState);
                throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
            }
        }

        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo userInfo = client.getUserInfo(code, receivedState);

        if (userInfo == null) {
            throw new BusinessException("소셜 로그인 정보를 가져오지 못했습니다.", ErrorCode.INVALID_OAUTH_TOKEN);
        }

        return processLogin(userInfo, userAgent);
    }

    @Override
    @Transactional
    public LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken, String userAgent) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo userInfo = client.getUserInfoByAccessToken(accessToken);

        if (userInfo == null) {
            throw new BusinessException("소셜 로그인 정보를 가져오지 못했습니다.", ErrorCode.INVALID_OAUTH_TOKEN);
        }

        return processLogin(userInfo, userAgent);
    }

    private LoginResponse processLogin(OAuth2UserInfo userInfo, String userAgent) {
        if (userInfo == null || userInfo.email() == null) {
            throw new BusinessException("소셜 로그인 정보 처리 중 오류가 발생했습니다 (이메일 정보 없음).", ErrorCode.OAUTH_COMMUNICATION_ERROR);
        }

        boolean isNewUser = !userRepository.existsByEmail(userInfo.email());
        User user = userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> registerNewUser(userInfo));

        String browser = parseBrowserFromUserAgent(userAgent);
        user.updateLoginHistory(browser);
        log.debug("소셜 로그인 기록 업데이트. 사용자 ID: {}", user.getId());

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        long refreshTokenValidityInSeconds = Duration.parse(refreshTokenExpirationIso).toSeconds();
        refreshTokenRepository.save(new RefreshToken(user.getEmail(), refreshToken, refreshTokenValidityInSeconds));
        log.info("Refresh Token이 Redis에 저장되었습니다. User: {}", user.getEmail());

        return new LoginResponse(accessToken, refreshToken, isNewUser, LoginResponse.UserInfo.from(user));
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
                .role(UserRole.ROLE_USER)
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

    private String parseBrowserFromUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) return "Unknown";
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) return "Chrome";
        if (userAgent.contains("Edg")) return "Edge";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) return "Safari";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "Internet Explorer";
        return "Other";
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
                .queryParam("scope", kakao.getScope())
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