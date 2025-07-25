package com.example.CineHive.controller.oauth;

import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.oauth.AccessTokenRequest;
import com.example.CineHive.entity.user.ProviderType;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.service.oauth.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "OAuth2 Controller", description = "통합 소셜 로그인 API")
@Validated
@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;
    private static final String STATE_SESSION_ATTRIBUTE_NAME = "oauthState";

    @Operation(summary = "소셜 로그인 페이지로 리다이렉트 (웹 전용)",
            description = """
            사용자를 각 소셜 로그인 플랫폼의 인증 페이지로 이동시킵니다.
            - **[사용법]** 클라이언트는 이 API를 직접 Ajax로 호출하는 것이 아니라, 로그인 버튼에 `<a>` 태그를 사용하여 이 API의 URL로 링크를 걸어야 합니다.
            - 예시: `<a href="/api/v1/oauth2/web/kakao">카카오로 로그인</a>`
            """)
    @GetMapping("/web/{platform}")
    public void redirectToProvider(
            @Parameter(description = "소셜 로그인 플랫폼", schema = @Schema(type = "string", allowableValues = {"naver", "kakao", "google"}))
            @PathVariable ProviderType platform,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        // CSRF 방어를 위한 state 생성 및 세션 저장
        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute(STATE_SESSION_ATTRIBUTE_NAME, state);

        // Service 계층에 Redirect URL 생성을 위임
        String redirectUrl = oauth2Service.getRedirectUrl(platform, state);
        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "소셜 로그인 콜백 처리 (웹 전용)",
            description = """
            각 플랫폼으로부터 인가 코드를 받아 로그인을 처리하고 서버의 JWT를 발급합니다.
            - **[사용법]** 이 API는 사용자가 직접 호출하는 것이 아니라, 소셜 플랫폼이 로그인 성공 후 사용자를 리다이렉션시키는 주소(Redirect URI)입니다.
            - 프론트엔드에서는 이 주소로 리다이렉션된 후, 응답받은 JWT를 저장하여 사용하면 됩니다.
            """)
    @GetMapping("/web/{platform}/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> handleCallback(
            @Parameter(description = "소셜 로그인 플랫폼") @PathVariable ProviderType platform,
            @Parameter(description = "플랫폼으로부터 발급받은 인가 코드") @RequestParam @NotBlank String code,
            @Parameter(description = "CSRF 방어용 상태 토큰") @RequestParam(required = false) String state,
            HttpSession session) {

        // CSRF 공격 방지를 위해 state 값 검증 (Kakao는 state를 사용하지 않으므로 예외 처리)
        if (platform != ProviderType.KAKAO) {
            String sessionState = (String) session.getAttribute(STATE_SESSION_ATTRIBUTE_NAME);
            if (sessionState == null || !sessionState.equals(state)) {
                // ErrorCode에 INVALID_STATE_VALUE 추가 필요
                throw new BusinessException("유효하지 않은 state 값입니다.", ErrorCode.INVALID_INPUT_VALUE);
            }
            session.removeAttribute(STATE_SESSION_ATTRIBUTE_NAME); // 사용한 state는 즉시 제거
        }

        LoginResponse loginResponse = oauth2Service.loginWithCode(platform, code, state);
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @Operation(summary = "소셜 로그인 (앱 전용)",
            description = """
            클라이언트 앱(iOS, Android)에서 SDK를 통해 직접 발급받은 액세스 토큰으로 로그인을 처리하고 서버의 JWT를 발급합니다.
            - **[흐름]** 1. 클라이언트 앱이 자체 SDK로 로그인 -> 2. 액세스 토큰 획득 -> 3. 이 API에 액세스 토큰 전달 -> 4. 서버 JWT 발급
            """)
    @PostMapping("/app/{platform}/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginFromApp(
            @Parameter(description = "소셜 로그인 플랫폼") @PathVariable ProviderType platform,
            @Valid @RequestBody AccessTokenRequest request) {

        LoginResponse loginResponse = oauth2Service.loginWithAccessToken(platform, request.accessToken());
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}
