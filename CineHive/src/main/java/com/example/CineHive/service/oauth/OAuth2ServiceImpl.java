package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
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
    private Map<ProviderType, OAuth2Client> clientMap;

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        clientMap = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuth2Client::getProviderType, Function.identity())
        );
    }

    @Override
    @Transactional
    public LoginResponse loginWithCode(ProviderType providerType, String code) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo userInfo = client.getUserInfo(code)
                .onErrorMap(error -> {
                    log.error("OAuth 통신 오류 (인가 코드 사용)", error);
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
                    log.error("OAuth 통신 오류 (액세스 토큰 사용)", error);
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

        return new LoginResponse(
                token,
                isNewUser,
                LoginResponse.UserInfo.from(user)
        );
    }

    private User registerNewUser(OAuth2UserInfo userInfo) {
        log.info("신규 소셜 사용자 가입: {}", userInfo.email());
        String nickname = resolveUniqueNickname(userInfo.nickname(), userInfo.providerType());

        User newUser = User.builder()
                .email(userInfo.email())
                .password("OAUTH_USER_NO_PASSWORD") // 소셜 로그인 사용자는 별도 비밀번호 없음
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
}