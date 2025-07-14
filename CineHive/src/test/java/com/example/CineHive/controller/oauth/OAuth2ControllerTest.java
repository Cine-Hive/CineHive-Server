package com.example.CineHive.controller.oauth;

import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.dto.oauth.AccessTokenRequest;
import com.example.CineHive.entity.member.*;
import com.example.CineHive.service.oauth.OAuth2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OAuth2Controller 통합 테스트")
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OAuth2Service oauth2Service;

    private LoginResponseDto createDummyLoginResponse() {
        Member dummyMember = Member.builder()
                .email("test@example.com")
                .password("OAUTH_MEMBER_DUMMY_PASSWORD")
                .name("테스트유저")
                .nickname("테스트닉네임")
                .gender(Gender.MALE)
                .genres(new HashSet<>(Collections.singletonList("액션")))
                .provider(ProviderType.KAKAO) // 소셜 로그인 상황 가정
                .role(MemberRole.ROLE_USER)
                .build();

        LoginResponseDto.MemberInfo memberInfo = new LoginResponseDto.MemberInfo(
                1L, // 임의의 ID
                dummyMember.getEmail(),
                dummyMember.getName(),
                dummyMember.getNickname(),
                dummyMember.getGender().name(),
                dummyMember.getGenres()
        );

        return new LoginResponseDto("dummy-jwt-token", false, memberInfo);
    }

    @Nested
    @DisplayName("소셜 로그인 페이지 리다이렉트 테스트 (웹용)")
    class RedirectToProvider {

        @Test
        @DisplayName("✅ 성공: 카카오 로그인 요청 시 302 리다이렉트 응답을 반환한다.")
        void redirectToProvider_kakao_success() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/kakao"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("kauth.kakao.com")));
        }

        @Test
        @DisplayName("✅ 성공: 네이버 로그인 요청 시 302 리다이렉트 응답을 반환한다.")
        void redirectToProvider_naver_success() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/naver"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("nid.naver.com")));
        }

        @Test
        @DisplayName("❌ 실패: 지원하지 않는 플랫폼 요청 시 400 Bad Request를 반환한다.")
        void redirectToProvider_fail_unsupportedPlatform() throws Exception {
            mockMvc.perform(get("/api/v1/oauth2/facebook"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("소셜 로그인 콜백 처리 테스트 (웹용)")
    class HandleCallback {

        @Test
        @DisplayName("✅ 성공: 유효한 인가 코드로 로그인 처리 후 200 응답과 JWT를 반환한다.")
        void handleCallback_success() throws Exception {
            // given
            String platform = "kakao";
            String code = "valid-authorization-code";
            LoginResponseDto dummyResponse = createDummyLoginResponse();

            given(oauth2Service.loginWithCode(eq(ProviderType.KAKAO), eq(code)))
                    .willReturn(dummyResponse);

            // when & then
            mockMvc.perform(get("/api/v1/oauth2/{platform}/callback", platform)
                            .param("code", code))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").value(dummyResponse.token()))
                    .andExpect(jsonPath("$.data.member.email").value(dummyResponse.member().email()));
        }

        @Test
        @DisplayName("❌ 실패: 외부 API 통신 실패 시 500 Internal Server Error를 반환한다.")
        void handleCallback_fail_externalApiError() throws Exception {
            // given
            String platform = "kakao";
            String code = "invalid-code";

            given(oauth2Service.loginWithCode(any(ProviderType.class), any(String.class)))
                    .willThrow(new RuntimeException("외부 API 통신 실패"));

            // when & then
            mockMvc.perform(get("/api/v1/oauth2/{platform}/callback", platform)
                            .param("code", code))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("소셜 로그인 테스트 (앱용)")
    class LoginFromApp {

        @Test
        @DisplayName("✅ 성공: 유효한 액세스 토큰으로 로그인 처리 후 200 응답과 JWT를 반환한다.")
        void loginFromApp_success() throws Exception {
            // given
            String platform = "google";
            AccessTokenRequest request = new AccessTokenRequest("valid-access-token");
            LoginResponseDto dummyResponse = createDummyLoginResponse();

            given(oauth2Service.loginWithAccessToken(eq(ProviderType.GOOGLE), eq(request.accessToken())))
                    .willReturn(dummyResponse);

            // when & then
            mockMvc.perform(post("/api/v1/oauth2/app/login/{platform}", platform)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").value(dummyResponse.token()));
        }
    }
}