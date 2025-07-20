package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2MemberInfo;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.ProviderType;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * OAuth2Service의 비즈니스 로직을 테스트합니다.
 * 외부 의존성(Repository, Util, Client)은 Mockito를 사용하여 모의 처리합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 단위 테스트")
class OAuth2ServiceTest {

    @InjectMocks
    private OAuth2ServiceImpl oauth2Service;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private List<OAuth2Client> clients; // 실제 client 리스트 대신 Mock 주입

    // 테스트에서 사용할 Mock Client
    private OAuth2Client mockKakaoClient;

    @BeforeEach
    void setUp() {
        // Mock Client 설정
        mockKakaoClient = mock(OAuth2Client.class);
        given(mockKakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);

        oauth2Service = new OAuth2ServiceImpl(List.of(mockKakaoClient), memberRepository, jwtUtil);
        oauth2Service.init();
    }

    private OAuth2MemberInfo createDummyOAuth2MemberInfo(String email, String nickname, ProviderType provider) {
        return new OAuth2MemberInfo(email, nickname, provider);
    }

    private User createDummyMember(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .provider(ProviderType.KAKAO)
                .build();
    }

    @Nested
    @DisplayName("소셜 로그인/회원가입 처리 (processLogin)")
    class ProcessLogin {

        @Test
        @DisplayName("✅ 성공: 기존 회원이 소셜 로그인 시, isNewMember=false 와 함께 JWT를 발급한다.")
        void login_withExistingMember_shouldSucceed() {
            // given
            String email = "test@kakao.com";
            String nickname = "기존유저";
            OAuth2MemberInfo oAuth2MemberInfo = createDummyOAuth2MemberInfo(email, nickname, ProviderType.KAKAO);
            User existingUser = createDummyMember(email, nickname);
            String dummyToken = "dummy-jwt-token";

            given(memberRepository.existsByEmail(email)).willReturn(true);
            given(memberRepository.findByEmail(email)).willReturn(Optional.of(existingUser));
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, "any-access-token");

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewMember()).isFalse();
            assertThat(response.token()).isEqualTo(dummyToken);
            assertThat(response.memberInfo().email()).isEqualTo(existingUser.getEmail());
            assertThat(response.memberInfo().nickname()).isEqualTo(existingUser.getNickname());
        }

        @Test
        @DisplayName("✅ 성공: 신규 회원이 소셜 로그인 시, isNewMember=true 와 함께 회원가입 후 JWT를 발급한다.")
        void login_withNewMember_shouldRegisterAndSucceed() {
            // given
            String email = "new@kakao.com";
            String nickname = "신규유저";
            OAuth2MemberInfo oAuth2MemberInfo = createDummyOAuth2MemberInfo(email, nickname, ProviderType.KAKAO);
            User newUser = createDummyMember(email, nickname);
            String dummyToken = "dummy-jwt-token";

            given(memberRepository.existsByEmail(email)).willReturn(false);
            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());
            given(memberRepository.existsByNickname(nickname)).willReturn(false);
            given(memberRepository.save(any(User.class))).willReturn(newUser);
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, "any-access-token");

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewMember()).isTrue();
            assertThat(response.token()).isEqualTo(dummyToken);
            assertThat(response.memberInfo().email()).isEqualTo(newUser.getEmail());

            verify(memberRepository).save(any(User.class));
        }

        @Test
        @DisplayName("✅ 성공: 신규 회원 가입 시 닉네임이 중복되면, (Provider N) 접미사를 붙여 유니크한 닉네임을 생성한다.")
        void register_withDuplicateNickname_shouldCreateUniqueNickname() {
            // given
            String email = "another@kakao.com";
            String originalNickname = "중복닉네임";
            OAuth2MemberInfo oAuth2MemberInfo = createDummyOAuth2MemberInfo(email, originalNickname, ProviderType.KAKAO);

            String firstAttempt = originalNickname;
            String secondAttempt = String.format("%s (%s %d)", originalNickname, ProviderType.KAKAO.name(), 1);
            User newUserWithResolvedNickname = createDummyMember(email, secondAttempt);
            String dummyToken = "dummy-jwt-token";

            given(memberRepository.existsByEmail(email)).willReturn(false);
            given(memberRepository.findByEmail(email)).willReturn(Optional.empty());
            given(memberRepository.existsByNickname(firstAttempt)).willReturn(true); // 첫 시도는 중복
            given(memberRepository.existsByNickname(secondAttempt)).willReturn(false); // 두 번째 시도는 성공
            given(memberRepository.save(any(User.class))).willReturn(newUserWithResolvedNickname);
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, "any-access-token");

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewMember()).isTrue();
            assertThat(response.memberInfo().nickname()).isEqualTo(secondAttempt);
        }
    }

    @Test
    @DisplayName("✅ loginWithAccessToken 호출 시, 올바른 Client의 메서드를 호출한다.")
    void loginWithAccessToken_shouldCallCorrectClient() {
        // given
        String accessToken = "valid-access-token";
        OAuth2MemberInfo dummyInfo = createDummyOAuth2MemberInfo("test@kakao.com", "test", ProviderType.KAKAO);
        given(mockKakaoClient.getMemberInfoByAccessToken(accessToken)).willReturn(Mono.just(dummyInfo));
        given(memberRepository.existsByEmail(anyString())).willReturn(true);
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(mock(User.class)));
        given(jwtUtil.generateToken(anyString())).willReturn("dummy-token");

        // when
        oauth2Service.loginWithAccessToken(ProviderType.KAKAO, accessToken);

        // then
        verify(mockKakaoClient).getMemberInfoByAccessToken(accessToken);
    }
}