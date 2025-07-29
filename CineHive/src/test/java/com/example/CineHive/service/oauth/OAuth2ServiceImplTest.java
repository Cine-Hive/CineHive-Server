package com.example.CineHive.service.oauth;

import com.example.CineHive.client.OAuth2Client;
import com.example.CineHive.config.OAuthProperties;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.oauth.OAuth2UserInfo;
import com.example.CineHive.entity.auth.RefreshToken;
import com.example.CineHive.entity.user.Gender;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.user.UserRole;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.auth.RefreshTokenRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
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

    // --- Mocks ---
    @Mock private OAuth2Client kakaoClient;
    @Mock private OAuth2Client naverClient;
    @Mock private OAuth2Client googleClient;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private OAuthProperties oAuthProperties;

    @BeforeEach
    void setUp() {
        // Mock 객체들을 사용하여 서비스 객체 생성
        oAuth2Service = new OAuth2ServiceImpl(
                List.of(kakaoClient, naverClient, googleClient),
                userRepository,
                refreshTokenRepository, // refreshTokenRepository 주입
                jwtTokenProvider,
                oAuthProperties
        );

        // @Value 필드 수동 주입
        ReflectionTestUtils.setField(oAuth2Service, "refreshTokenExpiration", 604800000L);

        // 각 클라이언트의 ProviderType 모킹
        given(kakaoClient.getProviderType()).willReturn(ProviderType.KAKAO);
        given(naverClient.getProviderType()).willReturn(ProviderType.NAVER);
        given(googleClient.getProviderType()).willReturn(ProviderType.GOOGLE);

        // 서비스의 init() 메서드를 수동으로 호출하여 clientMap 초기화
        oAuth2Service.init();
    }

    @Nested
    @DisplayName("loginWithCode 메서드는")
    class Describe_loginWithCode {
        private final String code = "test_code";
        private final String accessToken = "dummy-access-token";
        private final String refreshToken = "dummy-refresh-token";
        private final String testEmail = "test@example.com";
        private final String testNickname = "testUser";

        @Test
        @DisplayName("신규 사용자의 정보로 요청 시, 토큰들을 발급하고 Refresh Token을 저장한다")
        void forNewUser_shouldIssueTokensAndSaveRefreshToken() {
            // given
            OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().build();
            User savedUser = UserFixture.anUser().build();

            given(kakaoClient.getUserInfo(code, null)).willReturn(Mono.just(userInfo));
            given(userRepository.existsByEmail(testEmail)).willReturn(false);
            given(userRepository.findByEmail(testEmail)).willReturn(Optional.empty());
            given(userRepository.existsByNickname(testNickname)).willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtTokenProvider.createAccessToken(testEmail)).willReturn(accessToken);
            given(jwtTokenProvider.createRefreshToken(testEmail)).willReturn(refreshToken);

            // when
            LoginResponse response = oAuth2Service.loginWithCode(ProviderType.KAKAO, code, null, null);

            // then
            assertThat(response.isNewUser()).isTrue();
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo(refreshToken);

            // RefreshTokenRepository.save가 호출되었는지 검증
            ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            then(refreshTokenRepository).should().save(refreshTokenCaptor.capture());
            assertThat(refreshTokenCaptor.getValue().getEmail()).isEqualTo(testEmail);
            assertThat(refreshTokenCaptor.getValue().getToken()).isEqualTo(refreshToken);
        }

        @Test
        @DisplayName("기존 사용자의 정보로 요청 시, 새로운 토큰들을 발급하고 Refresh Token을 갱신한다")
        void forExistingUser_shouldIssueTokensAndUpdateRefreshToken() {
            // given
            OAuth2UserInfo userInfo = OAuth2UserInfoFixture.aUserInfo().build();
            User existingUser = UserFixture.anUser().build();

            given(kakaoClient.getUserInfo(code, null)).willReturn(Mono.just(userInfo));
            given(userRepository.existsByEmail(testEmail)).willReturn(true);
            given(userRepository.findByEmail(testEmail)).willReturn(Optional.of(existingUser));
            given(jwtTokenProvider.createAccessToken(testEmail)).willReturn(accessToken);
            given(jwtTokenProvider.createRefreshToken(testEmail)).willReturn(refreshToken);

            // when
            LoginResponse response = oAuth2Service.loginWithCode(ProviderType.KAKAO, code, null, null);

            // then
            assertThat(response.isNewUser()).isFalse();
            assertThat(response.accessToken()).isEqualTo(accessToken);
            assertThat(response.refreshToken()).isEqualTo(refreshToken);
            assertThat(response.userInfo().id()).isEqualTo(existingUser.getId());

            // DB에 새로운 유저가 저장되지 않았는지 검증
            then(userRepository).should(never()).save(any(User.class));
            // RefreshToken은 항상 새로 저장(갱신)되는지 검증
            then(refreshTokenRepository).should().save(any(RefreshToken.class));
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

        public OAuth2UserInfo build() {
            return new OAuth2UserInfo(email, nickname, providerType);
        }
    }

    static class UserFixture {
        private Long id = 1L;
        private String email = "test@example.com";
        private String nickname = "testUser";

        public static UserFixture anUser() {
            return new UserFixture();
        }

        public User build() {
            User user = User.builder()
                    .email(email)
                    .password("OAUTH_USER_NO_PASSWORD")
                    .name(nickname)
                    .nickname(nickname)
                    .gender(Gender.OTHER)
                    .genres(Collections.emptySet())
                    .provider(ProviderType.KAKAO)
                    .role(UserRole.ROLE_USER)
                    .build();
            ReflectionTestUtils.setField(user, "id", this.id);
            return user;
        }
    }
}
