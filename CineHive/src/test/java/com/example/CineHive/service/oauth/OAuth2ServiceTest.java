package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.exception.InvalidOAuthTokenException;
import com.example.CineHive.exception.OAuthCommunicationException;
import com.example.CineHive.repository.member.MemberRepository;
import com.example.CineHive.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 통합 테스트")
class OAuth2ServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private OAuth2Client kakaoClient;
    @Mock
    private OAuth2Client googleClient;

    @InjectMocks
    private OAuth2ServiceImpl oauth2Service;

    private OAuth2MemberInfo dummyMemberInfo;
    private Member existingMember;

    @BeforeEach
    void setUp() {
        // given - Mock 객체들의 기본 동작 설정
        given(kakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);
        given(googleClient.getProviderType()).willReturn(ProviderType.GOOGLE);

        // @InjectMocks가 생성자를 통해 주입하므로, List를 직접 생성하여 넘겨줌
        oauth2Service = new OAuth2ServiceImpl(List.of(kakaoClient, googleClient), memberRepository, jwtUtil);
        oauth2Service.init(); // @PostConstruct 수동 호출

        // given - 테스트용 기본 데이터
        dummyMemberInfo = new OAuth2MemberInfo("test@example.com", "테스트유저", ProviderType.KAKAO);
        existingMember = Member.builder()
                .email("test@example.com").password("PASSWORD").name("기존유저").nickname("기존유저")
                .gender(Gender.MALE).genres(new HashSet<>()).provider(ProviderType.KAKAO).role(MemberRole.ROLE_USER)
                .build();
    }

    @Nested
    @DisplayName("로그인 처리 (loginWithCode)")
    class ProcessLogin {
        @Nested
        @DisplayName("성공 시나리오")
        class Success {
            @Test
            @DisplayName("✅ 기존 회원이 로그인 시, 회원가입 없이 로그인 처리된다.")
            void forExistingMember() {
                // given
                String code = "valid-code";
                String token = "jwt-token";
                given(kakaoClient.getMemberInfo(code)).willReturn(Mono.just(dummyMemberInfo));
                given(memberRepository.findByEmail(dummyMemberInfo.email())).willReturn(Optional.of(existingMember));
                given(jwtUtil.generateToken(existingMember.getEmail())).willReturn(token);
                given(memberRepository.existsByEmail(dummyMemberInfo.email())).willReturn(true);

                // when
                LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, code);

                // then
                assertThat(response.isNewMember()).isFalse();
                assertThat(response.token()).isEqualTo(token);
                assertThat(response.member().email()).isEqualTo(existingMember.getEmail());
                verify(memberRepository, never()).save(any(Member.class));
                verify(kakaoClient, times(1)).getMemberInfo(eq(code));
            }

            @Test
            @DisplayName("✅ 신규 회원이 로그인 시, 회원가입 후 로그인 처리된다.")
            void forNewMember() {
                // given
                String code = "valid-code";
                given(kakaoClient.getMemberInfo(code)).willReturn(Mono.just(dummyMemberInfo));
                given(memberRepository.findByEmail(dummyMemberInfo.email())).willReturn(Optional.empty());
                given(memberRepository.save(any(Member.class))).willReturn(existingMember); // 저장 후 객체 반환 가정
                given(jwtUtil.generateToken(anyString())).willReturn("new-jwt-token");

                // when
                LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, code);

                // then
                assertThat(response.isNewMember()).isTrue();
                assertThat(response.token()).isEqualTo("new-jwt-token");
                verify(memberRepository, times(1)).save(any(Member.class));
            }
        }

        @Nested
        @DisplayName("실패 시나리오")
        class Failure {
            @Test
            @DisplayName("❌ 지원하지 않는 Provider 요청 시 IllegalArgumentException을 던진다.")
            void unsupportedProvider() {
                // given
                ProviderType unsupportedProvider = ProviderType.LOCAL;

                // when & then
                assertThatThrownBy(() -> oauth2Service.loginWithCode(unsupportedProvider, "any-code"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("지원하지 않는 소셜 로그인입니다");
            }

            @Test
            @DisplayName("❌ OAuth2Client가 Mono.error를 반환하면 OAuthCommunicationException을 던진다.")
            void clientReturnsError() {
                // given
                String code = "error-code";
                given(kakaoClient.getMemberInfo(code)).willReturn(Mono.error(new RuntimeException("API is down")));

                // when & then
                assertThatThrownBy(() -> oauth2Service.loginWithCode(ProviderType.KAKAO, code))
                        .isInstanceOf(OAuthCommunicationException.class)
                        .hasMessageContaining("소셜 프로필 정보를 가져오는 데 실패했습니다.");
            }

            @Test
            @DisplayName("❌ OAuth2Client가 Mono.empty를 반환하면 InvalidOAuthTokenException을 던진다.")
            void clientReturnsEmpty() {
                // given
                String code = "empty-code";
                given(kakaoClient.getMemberInfo(code)).willReturn(Mono.empty());

                // when & then
                assertThatThrownBy(() -> oauth2Service.loginWithCode(ProviderType.KAKAO, code))
                        .isInstanceOf(InvalidOAuthTokenException.class)
                        .hasMessageContaining("유효하지 않은 인가 코드입니다.");
            }
        }
    }

    @Nested
    @DisplayName("닉네임 중복 처리 (resolveNickname)")
    class ResolveNickname {
        @Test
        @DisplayName("✅ 닉네임이 2번 중복될 경우, 접미사 숫자가 2까지 증가한다.")
        void whenDuplicatedTwice() {
            // given
            String originalNickname = dummyMemberInfo.nickname();
            String firstAttempt = originalNickname + " (KAKAO 1)";
            String secondAttempt = originalNickname + " (KAKAO 2)";

            given(kakaoClient.getMemberInfo(anyString())).willReturn(Mono.just(dummyMemberInfo));
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));

            // Mocking the nickname existence checks
            given(memberRepository.existsByNickname(originalNickname)).willReturn(true);
            given(memberRepository.existsByNickname(firstAttempt)).willReturn(true);
            given(memberRepository.existsByNickname(secondAttempt)).willReturn(false);

            // when
            LoginResponseDto response = oauth2Service.loginWithCode(ProviderType.KAKAO, "any-code");

            // then
            assertThat(response.member().nickname()).isEqualTo(secondAttempt);
            verify(memberRepository, times(1)).existsByNickname(originalNickname);
            verify(memberRepository, times(1)).existsByNickname(firstAttempt);
            verify(memberRepository, times(1)).existsByNickname(secondAttempt);
        }
    }
}