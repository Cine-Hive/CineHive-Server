package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.entity.member.Gender;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.entity.member.MemberRole;
import com.example.CineHive.entity.member.ProviderType;
import com.example.CineHive.repository.member.MemberRepository;
import com.example.CineHive.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 테스트")
class OAuth2ServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock // 실제 외부 API를 호출하지 않도록 Mock 처리
    private OAuth2Client kakaoClient;

    @Spy // 실제 List 객체를 사용하되, Mockito가 감시하도록 설정
    private List<OAuth2Client> clients;

    @InjectMocks
    private OAuth2Service oauth2Service;

    private OAuth2MemberInfo dummyMemberInfo;
    private Member existingMember;
    @BeforeEach
    void setUp() {
        given(kakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);
        clients = List.of(kakaoClient);

        oauth2Service = new OAuth2Service(clients, memberRepository, jwtUtil);
        oauth2Service.init();

        dummyMemberInfo = new OAuth2MemberInfo("test@example.com", "테스트유저", ProviderType.KAKAO);

        // [수정] existingMember 생성 시 모든 필수 필드 설정
        existingMember = Member.builder()
                .email("test@example.com")
                .password("EXISTING_MEMBER_PASSWORD")
                .name("기존유저이름")
                .nickname("기존유저")
                .gender(Gender.MALE) // NullPointerException의 원인
                .genres(new HashSet<>(Collections.singletonList("드라마")))
                .provider(ProviderType.KAKAO)
                .role(MemberRole.ROLE_USER)
                .build();
    }

    @Nested
    @DisplayName("로그인 처리 (loginWithCode)")
    class ProcessLogin {

        @Test
        @DisplayName("✅ 성공: 기존 회원이 소셜 로그인 시, 회원가입 없이 로그인 처리된다.")
        void loginWithCode_forExistingMember() {
            // given
            String code = "test-code";
            String expectedToken = "dummy-jwt-token";

            given(kakaoClient.getMemberInfo(code)).willReturn(Mono.just(dummyMemberInfo));
            given(memberRepository.existsByEmail(dummyMemberInfo.email())).willReturn(true);
            given(memberRepository.findByEmail(dummyMemberInfo.email())).willReturn(Optional.of(existingMember));
            given(jwtUtil.generateToken(existingMember.getEmail())).willReturn(expectedToken);

            // when
            LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, code);

            // then
            assertThat(response.isNewMember()).isFalse();
            assertThat(response.token()).isEqualTo(expectedToken);
            assertThat(response.member().nickname()).isEqualTo("기존유저");

            verify(memberRepository, never()).save(any(Member.class));
        }

        @Test
        @DisplayName("✅ 성공: 신규 회원이 소셜 로그인 시, 회원가입 후 로그인 처리된다.")
        void loginWithCode_forNewMember() {
            // given
            String code = "test-code";
            String expectedToken = "new-member-token";

            // 신규 회원 시나리오를 위한 Member 객체 (save 메서드의 반환값으로 사용)
            Member savedMember = Member.builder()
                    .email(dummyMemberInfo.email())
                    .name(dummyMemberInfo.nickname())
                    .nickname(dummyMemberInfo.nickname())
                    .gender(Gender.OTHER)
                    .provider(dummyMemberInfo.providerType())
                    .role(MemberRole.ROLE_USER)
                    .genres(new HashSet<>())
                    .password("OAUTH_MEMBER_NO_PASSWORD")
                    .build();

            given(kakaoClient.getMemberInfo(code)).willReturn(Mono.just(dummyMemberInfo));
            given(memberRepository.existsByEmail(dummyMemberInfo.email())).willReturn(false);
            given(memberRepository.findByEmail(dummyMemberInfo.email())).willReturn(Optional.empty());
            given(memberRepository.existsByNickname(dummyMemberInfo.nickname())).willReturn(false);

            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
                Member memberToSave = invocation.getArgument(0);
                return memberToSave;
            });

            given(jwtUtil.generateToken(dummyMemberInfo.email())).willReturn(expectedToken);

            // when
            LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, code);

            // then
            assertThat(response.isNewMember()).isTrue();
            assertThat(response.token()).isEqualTo(expectedToken);
            assertThat(response.member().nickname()).isEqualTo("테스트유저");

            verify(memberRepository, times(1)).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("닉네임 중복 처리 (resolveNickname)")
    class ResolveNickname {

        @Test
        @DisplayName("✅ 성공: 닉네임이 중복될 경우, ' (PROVIDER 숫자)' 접미사를 붙여 반환한다.")
        void resolveNickname_whenDuplicated() {
            // given
            // "테스트유저"는 존재하고, "테스트유저 (KAKAO 1)"은 존재하지 않는다고 설정
            given(memberRepository.existsByNickname("테스트유저")).willReturn(true);
            given(memberRepository.existsByNickname("테스트유저 (KAKAO 1)")).willReturn(false);

            given(kakaoClient.getMemberInfo(anyString())).willReturn(Mono.just(dummyMemberInfo));
            given(memberRepository.existsByEmail(dummyMemberInfo.email())).willReturn(false);
            given(memberRepository.findByEmail(dummyMemberInfo.email())).willReturn(Optional.empty());
            given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, "any-code");

            // then
            assertThat(response.member().nickname()).isEqualTo("테스트유저 (KAKAO 1)");

            // existsByNickname이 총 2번 호출되었는지 검증 (원본 1번 + 접미사 붙인 것 1번)
            verify(memberRepository, times(2)).existsByNickname(anyString());
        }
    }
}