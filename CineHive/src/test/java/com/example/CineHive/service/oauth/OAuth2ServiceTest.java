package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.repository.user.UserRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 단위 테스트")
class OAuth2ServiceTest {

    @InjectMocks
    private OAuth2ServiceImpl oauth2Service;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    private OAuth2Client mockKakaoClient;

    @BeforeEach
    void setUp() {
        mockKakaoClient = mock(OAuth2Client.class);
        given(mockKakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);
        oauth2Service = new OAuth2ServiceImpl(List.of(mockKakaoClient), userRepository, jwtUtil);
        oauth2Service.init();
    }

    private OAuth2UserInfo createDummyOAuth2UserInfo(String email, String nickname, ProviderType provider) {
        return new OAuth2UserInfo(email, nickname, provider);
    }

    private User createDummyUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .provider(ProviderType.KAKAO)
                .build();
    }

    @Nested
    @DisplayName("소셜 로그인/회원가입 처리")
    class ProcessLogin {

        @Test
        @DisplayName("✅ 성공: 기존 사용자가 소셜 로그인 시, isNewUser=false 와 함께 JWT를 발급한다.")
        void login_withExistingUser_shouldSucceed() {
            // given
            String email = "test@kakao.com";
            String nickname = "기존유저";
            String accessToken = "any-access-token";
            OAuth2UserInfo oAuth2UserInfo = createDummyOAuth2UserInfo(email, nickname, ProviderType.KAKAO);
            User existingUser = createDummyUser(email, nickname);
            String dummyToken = "dummy-jwt-token";

            given(mockKakaoClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(oAuth2UserInfo));
            given(userRepository.existsByEmail(email)).willReturn(true);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(existingUser));
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, accessToken);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewUser()).isFalse();
            assertThat(response.token()).isEqualTo(dummyToken);
            assertThat(response.userInfo().email()).isEqualTo(existingUser.getEmail());
            assertThat(response.userInfo().nickname()).isEqualTo(existingUser.getNickname());
        }

        @Test
        @DisplayName("✅ 성공: 신규 사용자가 소셜 로그인 시, isNewUser=true 와 함께 회원가입 후 JWT를 발급한다.")
        void login_withNewUser_shouldRegisterAndSucceed() {
            // given
            String email = "new@kakao.com";
            String nickname = "신규유저";
            String accessToken = "any-access-token";
            OAuth2UserInfo oAuth2UserInfo = createDummyOAuth2UserInfo(email, nickname, ProviderType.KAKAO);
            User newUser = createDummyUser(email, nickname);
            String dummyToken = "dummy-jwt-token";

            given(mockKakaoClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(oAuth2UserInfo));
            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(userRepository.existsByNickname(nickname)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(newUser);
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, accessToken);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewUser()).isTrue();
            assertThat(response.token()).isEqualTo(dummyToken);
            assertThat(response.userInfo().email()).isEqualTo(newUser.getEmail());

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("✅ 성공: 신규 회원 가입 시 닉네임이 중복되면, (Provider N) 접미사를 붙여 유니크한 닉네임을 생성한다.")
        void register_withDuplicateNickname_shouldCreateUniqueNickname() {
            // given
            String email = "another@kakao.com";
            String originalNickname = "중복닉네임";
            String accessToken = "any-access-token";
            OAuth2UserInfo oAuth2UserInfo = createDummyOAuth2UserInfo(email, originalNickname, ProviderType.KAKAO);

            String firstAttempt = originalNickname;
            String secondAttempt = String.format("%s (%s %d)", originalNickname, ProviderType.KAKAO.name(), 1);
            User newUserWithResolvedNickname = createDummyUser(email, secondAttempt);
            String dummyToken = "dummy-jwt-token";

            given(mockKakaoClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(oAuth2UserInfo));
            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());
            given(userRepository.existsByNickname(firstAttempt)).willReturn(true);
            given(userRepository.existsByNickname(secondAttempt)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(newUserWithResolvedNickname);
            given(jwtUtil.generateToken(email)).willReturn(dummyToken);

            // when
            LoginResponse response = oauth2Service.loginWithAccessToken(ProviderType.KAKAO, accessToken);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isNewUser()).isTrue();
            assertThat(response.userInfo().nickname()).isEqualTo(secondAttempt);
        }
    }

    @Test
    @DisplayName("✅ loginWithAccessToken 호출 시, 올바른 Client의 메서드를 호출한다.")
    void loginWithAccessToken_shouldCallCorrectClient() {
        // given
        String accessToken = "valid-access-token";
        OAuth2UserInfo dummyInfo = createDummyOAuth2UserInfo("test@kakao.com", "test", ProviderType.KAKAO);
        User mockUser = createDummyUser("test@kakao.com", "test");

        given(mockKakaoClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(dummyInfo));
        given(userRepository.existsByEmail(anyString())).willReturn(true);
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(mockUser));
        given(jwtUtil.generateToken(anyString())).willReturn("dummy-token");

        // when
        oauth2Service.loginWithAccessToken(ProviderType.KAKAO, accessToken);

        // then
        verify(mockKakaoClient).getUserInfoByAccessToken(accessToken);
    }
}