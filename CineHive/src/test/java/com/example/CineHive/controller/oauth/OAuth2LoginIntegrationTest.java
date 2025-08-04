package com.example.CineHive.controller.oauth;

import com.example.CineHive.domain.user.enums.Gender;
import com.example.CineHive.domain.auth.enums.ProviderType;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("OAuth2 로그인 통합 테스트")
class OAuth2LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static MockWebServer mockWebServer;
    private MockHttpSession mockSession;

    @BeforeAll
    static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        mockSession = new MockHttpSession();
    }

    /**
     * MockWebServer의 동적 URL을 application.yml의 oauth2 프로퍼티 값으로 설정합니다.
     */
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        String baseUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        // Kakao
        registry.add("oauth2.kakao.token-uri", () -> baseUrl + "/oauth/token");
        registry.add("oauth2.kakao.user-info-uri", () -> baseUrl + "/v2/user/me");
        registry.add("oauth2.kakao.client-id", () -> "test-kakao-client-id");
        registry.add("oauth2.kakao.redirect-uri", () -> "http://localhost/api/v1/oauth2/web/kakao/callback");

        // Naver
        registry.add("oauth2.naver.token-uri", () -> baseUrl + "/oauth2.0/token");
        registry.add("oauth2.naver.user-info-uri", () -> baseUrl + "/v1/nid/me");
        registry.add("oauth2.naver.client-id", () -> "test-naver-client-id");
        registry.add("oauth2.naver.client-secret", () -> "test-naver-client-secret");
        registry.add("oauth2.naver.redirect-uri", () -> "http://localhost/api/v1/oauth2/web/naver/callback");

        // Google
        registry.add("oauth2.google.token-uri", () -> baseUrl + "/token");
        registry.add("oauth2.google.user-info-uri", () -> baseUrl + "/oauth2/v3/userinfo");
        registry.add("oauth2.google.client-id", () -> "test-google-client-id");
        registry.add("oauth2.google.client-secret", () -> "test-google-client-secret");
        registry.add("oauth2.google.redirect-uri", () -> "http://localhost/api/v1/oauth2/web/google/callback");
        registry.add("oauth2.google.scope", () -> "email profile");
    }

    @Nested
    @DisplayName("소셜 로그인 리다이렉션 API는 (/api/v1/oauth2/web/{platform})")
    class Describe_redirectToProvider {

        @Test
        @DisplayName("카카오 요청 시 카카오 인증 페이지로 리다이렉트한다")
        void whenKakao_thenRedirectsToKakao() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}", ProviderType.KAKAO)
                            .session(mockSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern(
                            "https://kauth.kakao.com/oauth/authorize*client_id=test-kakao-client-id*redirect_uri=http://localhost/api/v1/oauth2/web/kakao/callback*"
                    ));
        }

        @Test
        @DisplayName("네이버 요청 시 네이버 인증 페이지로 리다이렉트한다")
        void whenNaver_thenRedirectsToNaver() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}", ProviderType.NAVER)
                            .session(mockSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(result -> {
                        String url = result.getResponse().getRedirectedUrl();
                        String state = (String) result.getRequest().getSession().getAttribute("oauthState");
                        assertThat(url).startsWith("https://nid.naver.com/oauth2.0/authorize");
                        assertThat(url).contains("client_id=test-naver-client-id");
                        assertThat(url).contains("state=" + state);
                    });
        }

        @Test
        @DisplayName("구글 요청 시 구글 인증 페이지로 리다이렉트한다")
        void whenGoogle_thenRedirectsToGoogle() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}", ProviderType.GOOGLE)
                            .session(mockSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(result -> {
                        String url = result.getResponse().getRedirectedUrl();
                        String state = (String) result.getRequest().getSession().getAttribute("oauthState");
                        assertThat(url).startsWith("https://accounts.google.com/o/oauth2/v2/auth");
                        assertThat(url).contains("client_id=test-google-client-id");
                        assertThat(url).contains("scope=email profile");
                        assertThat(url).contains("state=" + state);
                    });
        }

        @Test
        @DisplayName("지원하지 않는 플랫폼(LOCAL) 요청 시 400 Bad Request를 반환한다")
        void whenUnsupportedPlatform_thenReturnsBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}", ProviderType.LOCAL)
                            .session(mockSession))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("C001"))
                    .andExpect(jsonPath("$.error.error").value("INVALID_INPUT_VALUE"));
        }
    }

    @Nested
    @DisplayName("카카오 로그인 콜백 API는 (/api/v1/oauth2/web/kakao/callback)")
    class Describe_handleKakaoCallback {

        private final String accessToken = "mock-kakao-access-token";
        private final String userEmail = "test_user@kakao.com";
        private final String userNickname = "테스트카카오유저";

        @Test
        @DisplayName("신규 사용자의 경우, DB에 사용자를 저장하고 isNewUser가 true인 응답을 반환한다")
        void forNewUser_shouldSaveUserAndReturnIsNewUserTrue() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody(accessToken)));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(12345L, userEmail, userNickname)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isNewUser").value(true))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));

            assertThat(userRepository.existsByEmail(userEmail)).isTrue();
        }

        @Test
        @DisplayName("기존 사용자의 경우, DB에 사용자를 저장하지 않고 isNewUser가 false인 응답을 반환한다")
        void forExistingUser_shouldNotSaveUserAndReturnIsNewUserFalse() throws Exception {
            userRepository.save(User.builder()
                    .email(userEmail)
                    .name("기존유저")
                    .nickname("기존유저")
                    .password("OAUTH_USER")
                    .provider(ProviderType.KAKAO)
                    .gender(Gender.OTHER)
                    .build());

            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody(accessToken)));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(54321L, userEmail, userNickname)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isNewUser").value(false))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));

            User found = userRepository.findByEmail(userEmail).orElseThrow();
            assertThat(found.getNickname()).isEqualTo("기존유저");
        }

        @Test
        @DisplayName("신규 가입 시 닉네임이 중복되면, '닉네임 (KAKAO 1)' 형태로 저장한다")
        void whenNicknameExists_shouldCreateUniqueNickname() throws Exception {
            // 이미 같은 닉네임 가진 사용자 등록
            userRepository.save(User.builder()
                    .email("another@user.com")
                    .name(userNickname)
                    .nickname(userNickname)
                    .password("password")
                    .provider(ProviderType.LOCAL)
                    .gender(Gender.OTHER)
                    .build());

            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody(accessToken)));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(12345L, userEmail, userNickname)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isNewUser").value(true))
                    .andExpect(jsonPath("$.data.userInfo.nickname")
                            .value(userNickname + " (KAKAO 1)"));

            User found = userRepository.findByEmail(userEmail).orElseThrow();
            assertThat(found.getNickname()).isEqualTo(userNickname + " (KAKAO 1)");
        }

        @Test
        @DisplayName("닉네임 중복이 2번 이상 발생하면, 숫자가 증가된 형태로 저장한다")
        void whenNicknameDuplicatedTwice_thenIncrementCounter() throws Exception {
            String base = "테스트카카오유저";
            // 이미 "테스트카카오유저", "테스트카카오유저 (KAKAO 1)" 두 사용자 존재
            userRepository.save(User.builder().email("u1@kakao.com").name(base).nickname(base).password("p").provider(ProviderType.LOCAL).gender(Gender.OTHER).build());
            userRepository.save(User.builder().email("u2@kakao.com").name(base).nickname(base + " (KAKAO 1)").password("p").provider(ProviderType.LOCAL).gender(Gender.OTHER).build());

            mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody("token")));
            mockWebServer.enqueue(new MockResponse().addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(999L, "new@kakao.com", base)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userInfo.nickname").value(base + " (KAKAO 2)"));
        }
    }

    @Nested
    @DisplayName("네이버 로그인 콜백 API는 (/api/v1/oauth2/web/naver/callback)")
    class Describe_handleNaverCallback {

        private final String accessToken = "mock-naver-access-token";
        private final String userEmail = "test_user@naver.com";
        private final String userNickname = "테스트네이버유저";
        private final String state = UUID.randomUUID().toString();

        @BeforeEach
        void setUp() {
            mockSession.setAttribute("oauthState", state);
        }

        @Test
        @DisplayName("신규 사용자의 경우, DB에 사용자를 저장하고 isNewUser가 true인 응답을 반환한다")
        void forNewUser_shouldSaveUserAndReturnIsNewUserTrue() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody(accessToken)));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createNaverUserInfoResponseBody("random_id", userEmail, userNickname)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.NAVER)
                            .param("code", "test_code")
                            .param("state", state)
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isNewUser").value(true))
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));

            assertThat(userRepository.existsByEmail(userEmail)).isTrue();
        }

        @Test
        @DisplayName("state 값이 일치하지 않으면, 401 Unauthorized 에러를 반환한다")
        void whenStateMismatches_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.NAVER)
                            .param("code", "test_code")
                            .param("state", "invalid_state")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("O003"))
                    .andExpect(jsonPath("$.error.error").value("INVALID_OAUTH_STATE"));
        }
    }

    @Nested
    @DisplayName("구글 로그인 콜백 API는 (/api/v1/oauth2/web/google/callback)")
    class Describe_handleGoogleCallback {

        private final String accessToken = "mock-google-access-token";
        private final String userEmail = "test_user@gmail.com";
        private final String userNickname = "테스트구글유저";
        private final String state = UUID.randomUUID().toString();

        @BeforeEach
        void setUp() {
            mockSession.setAttribute("oauthState", state);
        }

        @Test
        @DisplayName("신규 사용자의 경우, DB에 사용자를 저장하고 isNewUser가 true인 응답을 반환한다")
        void forNewUser_shouldSaveUserAndReturnIsNewUserTrue() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody(accessToken)));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createGoogleUserInfoResponseBody("random_id", userEmail, userNickname)));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.GOOGLE)
                            .param("code", "test_code")
                            .param("state", state)
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isNewUser").value(true))
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));

            assertThat(userRepository.existsByEmail(userEmail)).isTrue();
        }

        @Test
        @DisplayName("state 값이 없으면, 401 Unauthorized 에러를 반환한다")
        void whenStateIsMissing_shouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.GOOGLE)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("O003"))
                    .andExpect(jsonPath("$.error.error").value("INVALID_OAUTH_STATE"));

        }
    }

    @Nested
    @DisplayName("콜백 API 공통 예외 처리")
    class Describe_CallbackCommonExceptions {

        @Test
        @DisplayName("OAuth 서버 통신 실패 시, 503 Service Unavailable Error를 반환한다")
        void whenOAuthServerFails_shouldReturnServiceUnavailable() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody("any-token")));
            mockWebServer.enqueue(new MockResponse().setResponseCode(500));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.code").value("O002"))
                    .andExpect(jsonPath("$.error.error").value("OAUTH_COMMUNICATION_ERROR"));
        }

        @Test
        @DisplayName("사용자 정보에 이메일이 없으면, 503 Service Unavailable Error를 반환한다")
        void whenEmailIsMissing_shouldReturnServiceUnavailable() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createTokenResponseBody("any-token")));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(123L, null, "no-email-user")));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.code").value("O002"))
                    .andExpect(jsonPath("$.error.error").value("OAUTH_COMMUNICATION_ERROR"))
                    .andExpect(jsonPath("$.error.message")
                            .value("소셜 로그인 정보 처리 중 오류가 발생했습니다 (이메일 정보 없음)."));
        }

        @Test
        @DisplayName("토큰 요청 단계에서 401 응답 시, 503 Service Unavailable을 반환한다")
        void whenTokenRequestFails401_thenServiceUnavailable() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(401));

            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .param("code", "test_code")
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.code").value("O002"))
                    .andExpect(jsonPath("$.error.error").value("OAUTH_COMMUNICATION_ERROR"));
        }

        @Test
        @DisplayName("code 파라미터 누락 시, 400 Bad Request를 반환한다")
        void whenCodeMissing_thenBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/web/{platform}/callback", ProviderType.KAKAO)
                            .session(mockSession))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C005"))
                    .andExpect(jsonPath("$.error.error").value("MISSING_REQUEST_PARAMETER"));
        }
    }

    @Nested
    @DisplayName("앱 로그인 API는 (/api/v1/oauth2/app/{platform}/login)")
    class Describe_loginFromApp {

        private final String accessToken = "mock-app-access-token";
        private final String userEmail = "app_user@kakao.com";
        private final String userNickname = "앱카카오유저";

        @Test
        @DisplayName("유효한 Access Token으로 신규 사용자 로그인을 성공한다")
        void withValidTokenForNewUser_shouldSucceed() throws Exception {
            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(98765L, userEmail, userNickname)));

            String requestBody = objectMapper.writeValueAsString(Map.of("accessToken", accessToken));

            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.KAKAO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isNewUser").value(true))
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));

            assertThat(userRepository.existsByEmail(userEmail)).isTrue();
        }

        @Test
        @DisplayName("유효한 Access Token으로 기존 사용자 로그인을 성공한다")
        void withValidTokenForExistingUser_shouldSucceed() throws Exception {
            userRepository.save(User.builder()
                    .email(userEmail)
                    .name("기존앱유저")
                    .nickname("기존앱유저")
                    .password("OAUTH_USER")
                    .provider(ProviderType.KAKAO)
                    .gender(Gender.OTHER)
                    .build());

            mockWebServer.enqueue(new MockResponse()
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(createKakaoUserInfoResponseBody(98765L, userEmail, userNickname)));

            String requestBody = objectMapper.writeValueAsString(Map.of("accessToken", accessToken));

            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.KAKAO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isNewUser").value(false))
                    .andExpect(jsonPath("$.data.userInfo.email").value(userEmail));
        }

        @Test
        @DisplayName("유효하지 않은 Access Token으로 로그인 시 503 에러를 반환한다")
        void withInvalidToken_shouldFail() throws Exception {
            mockWebServer.enqueue(new MockResponse().setResponseCode(401));

            String requestBody = objectMapper.writeValueAsString(Map.of("accessToken", "invalid-token"));

            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.KAKAO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error.code").value("O002"))
                    .andExpect(jsonPath("$.error.error").value("OAUTH_COMMUNICATION_ERROR"));
        }

        @Test
        @DisplayName("지원하지 않는 플랫폼으로 앱 로그인 요청 시 400 Bad Request를 반환한다")
        void whenUnsupportedPlatformForAppLogin_thenBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.LOCAL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accessToken\":\"foo\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C001"))
                    .andExpect(jsonPath("$.error.error").value("INVALID_INPUT_VALUE"));
        }

        @Test
        @DisplayName("액세스 토큰 누락 시 400 Bad Request를 반환한다")
        void whenAccessTokenMissing_thenBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.KAKAO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C007"))
                    .andExpect(jsonPath("$.error.error").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("잘못된 JSON 키로 요청 시 400 Bad Request를 반환한다")
        void whenInvalidJsonField_thenBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/oauth2/app/{platform}/login", ProviderType.KAKAO)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"access_token\":\"foo\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("C007"))
                    .andExpect(jsonPath("$.error.error").value("VALIDATION_FAILED"));
        }


    }

    // --- Helper Methods ---

    private String createTokenResponseBody(String accessToken) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
                "token_type", "bearer",
                "access_token", accessToken,
                "expires_in", 86400,
                "refresh_token", "mock-refresh-token"
        );
        return objectMapper.writeValueAsString(body);
    }

    private String createKakaoUserInfoResponseBody(Long id, String email, String nickname) throws JsonProcessingException {
        Map<String, Object> properties = Map.of("nickname", nickname);
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("profile", properties);
        kakaoAccount.put("email", email);

        Map<String, Object> body = Map.of("id", id, "kakao_account", kakaoAccount);
        return objectMapper.writeValueAsString(body);
    }

    private String createNaverUserInfoResponseBody(String id, String email, String nickname) throws JsonProcessingException {
        Map<String, Object> response = Map.of("id", id, "email", email, "nickname", nickname);
        Map<String, Object> body = Map.of("resultcode", "00", "message", "success", "response", response);
        return objectMapper.writeValueAsString(body);
    }

    private String createGoogleUserInfoResponseBody(String sub, String email, String name) throws JsonProcessingException {
        Map<String, Object> body = Map.of("sub", sub, "email", email, "name", name);
        return objectMapper.writeValueAsString(body);
    }
}