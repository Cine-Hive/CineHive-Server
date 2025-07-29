package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.config.OAuthProperties;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.UserRole;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2ServiceImpl 단위 테스트")
class OAuth2ServiceImplTest {

    private OAuth2ServiceImpl oAuth2Service;

    @Mock private OAuth2Client kakaoClient;
    @Mock private OAuth2Client naverClient;
    @Mock private OAuth2Client googleClient;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private OAuthProperties oAuthProperties;

    @BeforeEach
    void setUp() {
        oAuth2Service = new OAuth2ServiceImpl(List.of(kakaoClient, naverClient, googleClient), userRepository, jwtTokenProvider, oAuthProperties);
        given(kakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);
        given(naverClient.getProviderType()).willReturn(ProviderType.NAVER);
        given(googleClient.getProviderType()).willReturn(ProviderType.GOOGLE);
        oAuth2Service.init();
    }

    @Nested
    @DisplayName("getRedirectUrl 메서드는")
    class Describe_getRedirectUrl {
        private final String state = "test_state";

        @Test
        @DisplayName("KAKAO Provider 요청 시, 올바른 인증 URL을 반환한다")
        void getRedirectUrl_ForKakao_ShouldReturnCorrectUrl() {
            // given
            OAuthProperties.Kakao kakaoProps = new OAuthProperties.Kakao();
            kakaoProps.setClientId("kakao_id");
            kakaoProps.setRedirectUri("kakao_uri");
            given(oAuthProperties.getKakao()).willReturn(kakaoProps);

            // when
            String redirectUrl = oAuth2Service.getRedirectUrl(ProviderType.KAKAO, state);

            // then
            assertThat(redirectUrl).contains("kauth.kakao.com", "client_id=kakao_id", "redirect_uri=kakao_uri");
        }

        @Test
        @DisplayName("NAVER Provider 요청 시, 올바른 인증 URL을 반환한다")
        void getRedirectUrl_ForNaver_ShouldReturnCorrectUrl() {
            // given
            OAuthProperties.Naver naverProps = new OAuthProperties.Naver();
            naverProps.setClientId("naver_id");
            naverProps.setRedirectUri("naver_uri");
            given(oAuthProperties.getNaver()).willReturn(naverProps);

            // when
            String redirectUrl = oAuth2Service.getRedirectUrl(ProviderType.NAVER, state);

            // then
            assertThat(redirectUrl).contains("nid.naver.com", "client_id=naver_id", "redirect_uri=naver_uri", "state=" + state);
        }

        @Test
        @DisplayName("GOOGLE Provider 요청 시, 올바른 인증 URL을 반환한다")
        void getRedirectUrl_ForGoogle_ShouldReturnCorrectUrl() {
            // given
            OAuthProperties.Google googleProps = new OAuthProperties.Google();
            googleProps.setClientId("google_id");
            googleProps.setRedirectUri("google_uri");
            googleProps.setScope("email profile");
            given(oAuthProperties.getGoogle()).willReturn(googleProps);

            // when
            String redirectUrl = oAuth2Service.getRedirectUrl(ProviderType.GOOGLE, state);

            // then
            assertThat(redirectUrl).contains("accounts.google.com", "client_id=google_id", "redirect_uri=google_uri", "scope=email profile", "state=" + state);
        }
    }

    @Nested
    @DisplayName("loginWithCode 메서드는")
    class Describe_loginWithCode {
        private final String code = "test_code";
        private final String jwtToken = "dummy-jwt-token";

        // 'loginWithCode'의 새로운 시그니처에 맞게 테스트를 수정합니다.
        // loginWithCode(ProviderType providerType, String code, String requestState, String sessionState)
        @Nested
        @DisplayName("NAVER/GOOGLE과 같이 state 값을 사용하는 플랫폼의 경우")
        class Context_with_state_provider {
            private final ProviderType providerType = ProviderType.NAVER;
            private final String state = "test_state";
            private final String testEmail = "naver@example.com";

            @Test
            @DisplayName("세션의 state와 전달받은 state가 일치하면, 정상적으로 로그인 처리한다")
            void withMatchingState_shouldLoginSuccessfully() {
                // given
                OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().email(testEmail).providerType(providerType).build();
                User existingUser = UserFixture.anUser().email(testEmail).provider(providerType).build();

                given(naverClient.getUserInfo(code, state)).willReturn(Mono.just(userInfo));
                given(userRepository.existsByEmail(testEmail)).willReturn(true);
                given(userRepository.findByEmail(testEmail)).willReturn(Optional.of(existingUser));
                given(jwtTokenProvider.generateToken(testEmail)).willReturn(jwtToken);

                // when
                LoginResponse response = oAuth2Service.loginWithCode(providerType, code, state, state);

                // then
                assertThat(response.isNewUser()).isFalse();
                assertThat(response.token()).isEqualTo(jwtToken);
                then(naverClient).should().getUserInfo(code, state);
            }

            @Test
            @DisplayName("세션의 state와 전달받은 state가 일치하지 않으면, INVALID_OAUTH_STATE 예외를 던진다")
            void withMismatchedState_shouldThrowException() {
                // given
                String sessionState = "session_state_value";
                String requestState = "mismatched_request_state";

                // when & then
                assertThatThrownBy(() -> oAuth2Service.loginWithCode(providerType, code, requestState, sessionState))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH_STATE);

                then(naverClient).should(never()).getUserInfo(any(), any());
            }
        }

        @Nested
        @DisplayName("KAKAO와 같이 state 값을 사용하지 않는 플랫폼의 경우")
        class Context_with_kakao_provider {
            private final ProviderType providerType = ProviderType.KAKAO;
            private final String testEmail = "kakao@example.com";
            private final String testNickname = "kakaoUser";

            @Test
            @DisplayName("기존 사용자의 정보로 요청 시, isNewUser가 false인 LoginResponse를 반환한다")
            void forExistingUser_shouldReturnLoginResponseWithIsNewUserFalse() {
                // given
                OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().email(testEmail).nickname(testNickname).build();
                User existingUser = UserFixture.anUser().email(testEmail).nickname(testNickname).build();

                given(kakaoClient.getUserInfo(code, null)).willReturn(Mono.just(userInfo));
                given(userRepository.existsByEmail(testEmail)).willReturn(true);
                given(userRepository.findByEmail(testEmail)).willReturn(Optional.of(existingUser));
                given(jwtTokenProvider.generateToken(testEmail)).willReturn(jwtToken);

                // when
                LoginResponse response = oAuth2Service.loginWithCode(providerType, code, null, null);

                // then
                assertThat(response.isNewUser()).isFalse();
                assertThat(response.token()).isEqualTo(jwtToken);
                then(userRepository).should(never()).save(any(User.class));
            }

            @Test
            @DisplayName("신규 사용자의 정보로 요청 시, isNewUser가 true인 LoginResponse를 반환하고 사용자를 저장한다")
            void forNewUser_shouldReturnLoginResponseWithIsNewUserTrueAndSaveUser() {
                // given
                OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().email(testEmail).nickname(testNickname).build();
                User savedUser = UserFixture.anUser().email(testEmail).nickname(testNickname).build();

                given(kakaoClient.getUserInfo(code, null)).willReturn(Mono.just(userInfo));
                given(userRepository.existsByEmail(testEmail)).willReturn(false);
                given(userRepository.findByEmail(testEmail)).willReturn(Optional.empty());
                given(userRepository.existsByNickname(testNickname)).willReturn(false);
                given(userRepository.save(any(User.class))).willReturn(savedUser);
                given(jwtTokenProvider.generateToken(testEmail)).willReturn(jwtToken);

                // when
                LoginResponse response = oAuth2Service.loginWithCode(providerType, code, null, null);

                // then
                assertThat(response.isNewUser()).isTrue();
                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
                then(userRepository).should().save(userCaptor.capture());
                assertThat(userCaptor.getValue().getEmail()).isEqualTo(testEmail);
            }
        }

        @Test
        @DisplayName("OAuth 서버 통신 중 오류가 발생하면, OAUTH_COMMUNICATION_ERROR 예외를 던진다")
        void whenClientReturnsError_shouldThrowException() {
            // given
            ProviderType providerType = ProviderType.KAKAO;
            given(kakaoClient.getUserInfo(code, null)).willReturn(Mono.error(new RuntimeException("Communication Error")));

            // when & then
            assertThatThrownBy(() -> oAuth2Service.loginWithCode(providerType, code, null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OAUTH_COMMUNICATION_ERROR);
        }
    }

    @Nested
    @DisplayName("loginWithAccessToken 메서드는")
    class Describe_loginWithAccessToken {
        private final ProviderType providerType = ProviderType.GOOGLE;
        private final String accessToken = "test_access_token";
        private final String testEmail = "test@gmail.com";
        private final String testNickname = "googleUser";
        private final String jwtToken = "dummy-jwt-token";

        @Test
        @DisplayName("기존 사용자의 정보로 요청 시, isNewUser가 false인 LoginResponse를 반환한다")
        void forExistingUser_ShouldReturnLoginResponseWithIsNewUserFalse() {
            // given
            OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().email(testEmail).nickname(testNickname).providerType(providerType).build();
            User existingUser = UserFixture.anUser().email(testEmail).nickname(testNickname).provider(providerType).build();

            given(googleClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(userInfo));
            given(userRepository.existsByEmail(testEmail)).willReturn(true);
            given(userRepository.findByEmail(testEmail)).willReturn(Optional.of(existingUser));
            given(jwtTokenProvider.generateToken(testEmail)).willReturn(jwtToken);

            // when
            LoginResponse response = oAuth2Service.loginWithAccessToken(providerType, accessToken);

            // then
            assertThat(response.isNewUser()).isFalse();
            assertThat(response.token()).isEqualTo(jwtToken);
            then(userRepository).should(never()).save(any(User.class));
        }

        @Test
        @DisplayName("신규 사용자의 정보로 요청 시, isNewUser가 true인 LoginResponse를 반환하고 사용자를 저장한다")
        void forNewUser_ShouldReturnLoginResponseWithIsNewUserTrueAndSaveUser() {
            // given
            OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().email(testEmail).nickname(testNickname).providerType(providerType).build();
            User savedUser = UserFixture.anUser().email(testEmail).nickname(testNickname).provider(providerType).build();

            given(googleClient.getUserInfoByAccessToken(accessToken)).willReturn(Mono.just(userInfo));
            given(userRepository.existsByEmail(testEmail)).willReturn(false);
            given(userRepository.findByEmail(testEmail)).willReturn(Optional.empty());
            given(userRepository.existsByNickname(testNickname)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenProvider.generateToken(testEmail)).willReturn(jwtToken);

            // when
            LoginResponse response = oAuth2Service.loginWithAccessToken(providerType, accessToken);

            // then
            assertThat(response.isNewUser()).isTrue();
            then(userRepository).should().save(any(User.class));
        }
    }

    // --- Test Fixture Builders ---

    static class OAuth2UserInfoFixture {
        private String email = "test@example.com";
        private String nickname = "testUser";
        private ProviderType providerType = ProviderType.KAKAO;

        public static OAuth2UserInfoFixture aUserInfo() {
            return new OAuth2UserInfoFixture();
        }

        public OAuth2UserInfoFixture email(String email) {
            this.email = email;
            return this;
        }

        public OAuth2UserInfoFixture nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public OAuth2UserInfoFixture providerType(ProviderType providerType) {
            this.providerType = providerType;
            return this;
        }

        public OAuth2UserInfo build() {
            return new OAuth2UserInfo(email, nickname, providerType);
        }
    }

    static class UserFixture {
        private Long id = 1L;
        private String email = "test@example.com";
        private String password = "OAUTH_USER_NO_PASSWORD";
        private String name = "testUser";
        private String nickname = "testUser";
        private Gender gender = Gender.OTHER;
        private ProviderType provider = ProviderType.KAKAO;
        private UserRole role = UserRole.ROLE_USER;

        public static UserFixture anUser() {
            return new UserFixture();
        }

        public UserFixture id(Long id) {
            this.id = id;
            return this;
        }

        public UserFixture email(String email) {
            this.email = email;
            return this;
        }

        public UserFixture name(String name) {
            this.name = name;
            return this;
        }

        public UserFixture nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserFixture provider(ProviderType provider) {
            this.provider = provider;
            return this;
        }

        public User build() {
            User user = User.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .nickname(nickname)
                    .gender(gender)
                    .genres(Collections.emptySet())
                    .provider(provider)
                    .role(role)
                    .build();

            try {
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, this.id);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to set user ID for test", e);
            }
            return user;
        }
    }
}
