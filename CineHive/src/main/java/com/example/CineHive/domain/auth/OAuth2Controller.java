package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.auth.dto.LoginResponse;
import com.example.CineHive.domain.auth.dto.AccessTokenRequest;
import com.example.CineHive.domain.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
            ### **웹 브라우저에서 소셜 로그인을 시작하는 첫 단계입니다.**
            
            클라이언트 개발자는 이 API를 직접 호출하는 것이 아니라, 사용자가 클릭할 수 있는 링크를 제공해야 합니다.
            
            **[클라이언트의 역할]**
            1.  웹 페이지에 '카카오로 로그인', '네이버로 로그인'과 같은 버튼을 만듭니다.
            2.  이 버튼들을 각각 아래와 같은 URL로 연결되는 `<a>` 태그로 감싸줍니다.
                - `<a href="/api/v1/oauth2/kakao/redirect">카카오로 로그인</a>`
                - `<a href="/api/v1/oauth2/naver/redirect">네이버로 로그인</a>`
                - `<a href="/api/v1/oauth2/google/redirect">구글로 로그인</a>`
            
            **[사용자 흐름]**
            1.  사용자가 위 링크 중 하나를 클릭합니다.
            2.  사용자의 브라우저는 해당 소셜 플랫폼의 공식 로그인 페이지로 자동 이동(리다이렉트)됩니다.
            3.  사용자가 해당 플랫폼의 아이디와 비밀번호로 로그인을 성공적으로 마칩니다.
            4.  로그인이 완료되면, 해당 플랫폼은 사용자를 우리 서비스의 **'콜백 API'**(`.../callback`)로 다시 돌려보냅니다.
            
            **※ 참고:** 이 과정은 OAuth2의 표준 'Authorization Code Grant' 흐름이며, 서버는 CSRF 공격 방어를 위한 보안 처리를 자동으로 수행합니다.
            """)
    @GetMapping("/{provider}/redirect")
    public void redirectToProvider(
            @Parameter(description = "소셜 로그인 제공업체 (e.g., naver, kakao, google)")
            @PathVariable("provider") ProviderType provider,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute(STATE_SESSION_ATTRIBUTE_NAME, state);

        String redirectUrl = oauth2Service.getRedirectUrl(provider, state);
        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "소셜 로그인 콜백 처리 (웹 전용)",
            description = """
            ### **소셜 플랫폼에서의 로그인이 성공한 후, 사용자가 최종적으로 도착하는 지점입니다.**
            
            이 API는 클라이언트가 직접 호출하는 것이 **절대 아닙니다.** 사용자가 소셜 플랫폼에서 로그인을 성공하면, 해당 플랫폼이 사용자의 브라우저를 이 주소로 자동으로 리다이렉트시킵니다.
            
            **[서버의 역할]**
            1.  소셜 플랫폼이 URL에 임시 `code`를 붙여서 보내줍니다.
            2.  우리 서버는 이 `code`를 사용하여 해당 플랫폼에 사용자 정보를 요청하고, CineHive 서비스의 회원가입 또는 로그인 처리를 완료합니다.
            3.  **성공 시, 사용자의 로그인 이력(브라우저 정보 등)이 기록됩니다.**
            4.  모든 처리가 성공하면, 서버는 CineHive 서비스에서 사용할 수 있는 자체 **JWT(JSON Web Token)**를 생성하여 응답 본문에 담아 반환합니다.
            
            **[클라이언트의 역할]**
            1.  이 API가 호출된 후, 프론트엔드 코드는 이 API의 **응답 본문(Response Body)**에서 `token`, `isNewUser`, `userInfo`가 포함된 JSON 데이터를 받게 됩니다.
            2.  여기서 가장 중요한 `token` 값을 **추출하여 브라우저의 안전한 공간(예: localStorage, 쿠키)에 저장**해야 합니다.
            3.  이후 CineHive의 다른 모든 API를 호출할 때는, HTTP 요청 헤더의 `Authorization` 필드에 이 토큰을 `Bearer <토큰값>` 형식으로 포함하여 보내야 합니다.
            """)
    @GetMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<LoginResponse>> handleCallback(
            @Parameter(description = "소셜 로그인 제공업체") @PathVariable("provider") ProviderType provider,
            @Parameter(description = "플랫폼으로부터 발급받은 인가 코드") @RequestParam @NotBlank String code,
            @Parameter(description = "CSRF 방어용 상태 토큰") @RequestParam(name = "state", required = false) String receivedState,
            @Parameter(name = "User-Agent", description = "로그인 이력 기록을 위한 클라이언트의 브라우저 정보", in = ParameterIn.HEADER, required = true)
            @RequestHeader("User-Agent") String userAgent,
            HttpSession session) {

        String sessionState = (String) session.getAttribute(STATE_SESSION_ATTRIBUTE_NAME);
        session.removeAttribute(STATE_SESSION_ATTRIBUTE_NAME);

        LoginResponse loginResponse = oauth2Service.loginWithCode(provider, code, receivedState, sessionState, userAgent);

        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @Operation(summary = "소셜 로그인 (앱 전용)",
            description = """
            ### **네이티브 모바일 앱(iOS, Android) 전용 소셜 로그인 API입니다.**
            
            웹 로그인과 달리, 앱에서는 각 플랫폼이 제공하는 SDK를 통해 로그인을 먼저 수행하고, 그 결과로 받은 **'액세스 토큰'**을 이 API로 전달해야 합니다.
            
            **[전체 흐름]**
            1.  **[앱]** 사용자가 앱 내의 '카카오로 로그인' 버튼을 누릅니다.
            2.  **[앱]** 카카오 SDK가 실행되어 로그인 과정을 처리합니다.
            3.  **[앱]** 로그인 성공 시, 카카오 SDK는 앱에게 **해당 플랫폼의 액세스 토큰**을 반환합니다.
            4.  **[앱]** 앱은 방금 받은 액세스 토큰을 담아, **이 API(`POST /api/v1/oauth2/{provider}/token`)를 호출**합니다.
            5.  **[서버]** 서버는 전달받은 액세스 토큰의 유효성을 검증하고, 사용자 정보를 받아 CineHive 서비스의 로그인/회원가입을 처리 및 **로그인 이력을 기록**합니다.
            6.  **[서버]** 모든 처리가 성공하면, 서버는 CineHive 서비스 전용 **JWT**를 생성하여 앱에게 응답으로 보내줍니다.
            7.  **[앱]** 앱은 서버로부터 받은 **CineHive JWT**를 안전하게 저장한 후, 앞으로 모든 서버 API 요청에 이 토큰을 사용합니다.
            
            **[요청 형식]**
            - `POST` 메서드를 사용해야 합니다.
            - `Content-Type`은 `application/json`이어야 합니다.
            - 요청 본문(Request Body)에는 아래와 같은 JSON 형식을 따라야 합니다.
              ```json
              {
                "accessToken": "소셜_플랫폼_SDK로부터_받은_액세스_토큰_값"
              }
              ```
            """)
    @PostMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<LoginResponse>> loginFromApp(
            @Parameter(description = "소셜 로그인 제공업체") @PathVariable("provider") ProviderType provider,
            @Valid @RequestBody AccessTokenRequest request,
            @Parameter(name = "User-Agent", description = "로그인 이력 기록을 위한 클라이언트의 앱 정보", in = ParameterIn.HEADER, required = true)
            @RequestHeader("User-Agent") String userAgent) {

        LoginResponse loginResponse = oauth2Service.loginWithAccessToken(provider, request.accessToken(), userAgent);
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}