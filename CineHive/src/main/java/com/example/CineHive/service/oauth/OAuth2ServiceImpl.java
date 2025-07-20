package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.ProviderType;
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
        this.clientMap = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuth2Client::getProviderType, Function.identity())
        );
    }

    @Override
    @Transactional
    public LoginResponse loginWithCode(ProviderType providerType, String code) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo memberInfo = client.getMemberInfo(code)
                .onErrorMap(error -> {
                    log.error("OAuth 통신 오류 발생 (인가 코드 사용)", error);
                    return new BusinessException(ErrorCode.OAUTH_COMMUNICATION_ERROR);
                })
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN)))
                .block();
        return processLogin(memberInfo);
    }

    @Override
    @Transactional
    public LoginResponse loginWithAccessToken(ProviderType providerType, String accessToken) {
        OAuth2Client client = getClient(providerType);
        OAuth2UserInfo memberInfo = client.getMemberInfoByAccessToken(accessToken)
                .onErrorMap(error -> {
                    log.error("OAuth 통신 오류 발생 (액세스 토큰 사용)", error);
                    return new BusinessException(ErrorCode.OAUTH_COMMUNICATION_ERROR);
                })
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.INVALID_OAUTH_TOKEN)))
                .block();
        return processLogin(memberInfo);
    }

    private LoginResponse processLogin(OAuth2UserInfo oAuth2UserInfo) {
        if (oAuth2UserInfo == null || oAuth2UserInfo.email() == null) {
            throw new BusinessException("소셜 로그인 정보를 처리하는 중 오류가 발생했습니다. (이메일 정보 없음)", ErrorCode.OAUTH_COMMUNICATION_ERROR);
        }

        boolean isNewMember = !userRepository.existsByEmail(oAuth2UserInfo.email());

        User user = userRepository.findByEmail(oAuth2UserInfo.email())
                .orElseGet(() -> registerNewMember(oAuth2UserInfo));

        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                isNewMember,
                LoginResponse.MemberInfo.from(user)
        );
    }

    private User registerNewMember(OAuth2UserInfo oAuth2UserInfo) {
        log.info("신규 소셜 회원 가입: {}", oAuth2UserInfo.email());
        String nickname = resolveUniqueNickname(oAuth2UserInfo.nickname(), oAuth2UserInfo.providerType());

        User newUser = User.builder()
                .email(oAuth2UserInfo.email())
                .password("OAUTH_MEMBER_NO_PASSWORD") // 소셜 로그인 회원은 별도 비밀번호 없음
                .name(oAuth2UserInfo.nickname())
                .nickname(nickname)
                .gender(Gender.OTHER) // 소셜 로그인 시 성별 정보는 기본값으로 설정
                .genres(Collections.emptySet())
                .provider(oAuth2UserInfo.providerType())
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
