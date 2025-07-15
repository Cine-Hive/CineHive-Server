package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.entity.member.Gender;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.entity.member.ProviderType;
import com.example.CineHive.exception.InvalidOAuthTokenException;
import com.example.CineHive.exception.OAuthCommunicationException;
import com.example.CineHive.repository.member.MemberRepository;
import com.example.CineHive.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        this.clientMap = clients.stream().collect(
                Collectors.toUnmodifiableMap(OAuth2Client::getProviderType, Function.identity())
        );
    }

    @Override
    @Transactional
    public LoginResponseDto loginWithCode(ProviderType providerType, String code) {
        OAuth2Client client = getClient(providerType);
        OAuth2MemberInfo memberInfo = client.getMemberInfo(code)
                .onErrorResume(error -> Mono.error(new OAuthCommunicationException("소셜 프로필 정보를 가져오는 데 실패했습니다.")))
                .switchIfEmpty(Mono.error(new InvalidOAuthTokenException("유효하지 않은 인가 코드입니다.")))
                .block();
        return processLogin(memberInfo);
    }

    @Override
    @Transactional
    public LoginResponseDto loginWithAccessToken(ProviderType providerType, String accessToken) {
        OAuth2Client client = getClient(providerType);
        OAuth2MemberInfo memberInfo = client.getMemberInfoByAccessToken(accessToken)
                .onErrorResume(error -> Mono.error(new OAuthCommunicationException("소셜 프로필 정보를 가져오는 데 실패했습니다.")))
                .switchIfEmpty(Mono.error(new InvalidOAuthTokenException("유효하지 않은 액세스 토큰입니다.")))
                .block();
        return processLogin(memberInfo);
    }

    /**
     * [공통 로직] 로그인/회원가입 처리 및 JWT 발급
     */
    private LoginResponseDto processLogin(OAuth2MemberInfo memberInfo) {
        if (memberInfo == null) {
            throw new OAuthCommunicationException("소셜 로그인 정보를 처리하는 중 오류가 발생했습니다.");
        }

        boolean isNewMember = !memberRepository.existsByEmail(memberInfo.email());

        Member member = memberRepository.findByEmail(memberInfo.email())
                .orElseGet(() -> registerNewMember(memberInfo));

        String token = jwtUtil.generateToken(member.getEmail());

        return LoginResponseDto.builder()
                .token(token)
                .isNewMember(isNewMember)
                .member(new LoginResponseDto.MemberInfo(member))
                .build();
    }

    private Member registerNewMember(OAuth2MemberInfo memberInfo) {
        log.info("신규 소셜 회원 가입: {}", memberInfo.email());
        String nickname = resolveNickname(memberInfo.nickname(), memberInfo.providerType());

        Member newMember = Member.builder()
                .email(memberInfo.email())
                .password("OAUTH_MEMBER_NO_PASSWORD")
                .nickname(nickname)
                .name(memberInfo.nickname())
                .gender(Gender.OTHER)
                .provider(memberInfo.providerType())
                .build();

        return memberRepository.save(newMember);
    }

    private String resolveNickname(String nickname, ProviderType providerType) {
        String resolvedNickname = nickname;
        int suffix = 1;
        while (memberRepository.existsByNickname(resolvedNickname)) {
            resolvedNickname = String.format("%s (%s %d)", nickname, providerType.name(), suffix++);
        }
        return resolvedNickname;
    }

    private OAuth2Client getClient(ProviderType providerType) {
        OAuth2Client client = clientMap.get(providerType);
        if (client == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + providerType);
        }
        return client;
    }
}