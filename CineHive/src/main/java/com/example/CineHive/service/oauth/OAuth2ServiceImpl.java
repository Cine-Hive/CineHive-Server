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
                .onErrorMap(error -> new OAuthCommunicationException("소셜 프로필 정보를 가져오는 데 실패했습니다.", error))
                .switchIfEmpty(Mono.error(new InvalidOAuthTokenException("유효하지 않은 인가 코드입니다.")))
                .block();
        return processLogin(memberInfo);
    }

    @Override
    @Transactional
    public LoginResponseDto loginWithAccessToken(ProviderType providerType, String accessToken) {
        OAuth2Client client = getClient(providerType);
        OAuth2MemberInfo memberInfo = client.getMemberInfoByAccessToken(accessToken)
                .onErrorMap(error -> new OAuthCommunicationException("소셜 프로필 정보를 가져오는 데 실패했습니다.", error))
                .switchIfEmpty(Mono.error(new InvalidOAuthTokenException("유효하지 않은 액세스 토큰입니다.")))
                .block();
        return processLogin(memberInfo);
    }

    private LoginResponseDto processLogin(OAuth2MemberInfo oAuth2MemberInfo) {
        if (oAuth2MemberInfo == null || oAuth2MemberInfo.email() == null) {
            throw new OAuthCommunicationException("소셜 로그인 정보를 처리하는 중 오류가 발생했습니다. (이메일 정보 없음)");
        }

        boolean isNewMember = !memberRepository.existsByEmail(oAuth2MemberInfo.email());

        Member member = memberRepository.findByEmail(oAuth2MemberInfo.email())
                .orElseGet(() -> registerNewMember(oAuth2MemberInfo));

        String token = jwtUtil.generateToken(member.getEmail());

        return new LoginResponseDto(
                token,
                isNewMember,
                LoginResponseDto.MemberInfo.from(member)
        );
    }

    private Member registerNewMember(OAuth2MemberInfo oAuth2MemberInfo) {
        log.info("신규 소셜 회원 가입: {}", oAuth2MemberInfo.email());
        String nickname = resolveUniqueNickname(oAuth2MemberInfo.nickname(), oAuth2MemberInfo.providerType());

        Member newMember = Member.builder()
                .email(oAuth2MemberInfo.email())
                .password("OAUTH_MEMBER_NO_PASSWORD")
                .name(oAuth2MemberInfo.nickname())
                .nickname(nickname)
                .gender(Gender.OTHER)
                .genres(Collections.emptySet())
                .provider(oAuth2MemberInfo.providerType())
                .build();

        return memberRepository.save(newMember);
    }

    private String resolveUniqueNickname(String nickname, ProviderType providerType) {
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