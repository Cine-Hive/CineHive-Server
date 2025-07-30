package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.auth.dto.*;
import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.MessageResponse;
import com.example.CineHive.domain.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증(회원가입, 로그인) 및 토큰 관리 API 컨트롤러입니다.
 */
@Tag(name = "Auth Controller", description = "인증(회원가입, 로그인) 및 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "회원가입",
            description = "새로운 사용자를 시스템에 등록합니다. 성공 시 `201 CREATED` 상태 코드와 성공 메시지를 반환합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new MessageResponse("회원가입이 성공적으로 완료되었습니다.")));
    }

    @Operation(summary = "로그인",
            description = """
            ### **일반 로그인 후, 서비스 이용에 필요한 토큰들을 발급받습니다.**
            
            **[서버 처리]**
            1.  로그인 성공 시, 서버는 두 종류의 토큰을 발급합니다.
                - **Access Token**: API 호출 시 사용 (수명: 30분)
                - **Refresh Token**: Access Token 재발급 시 사용 (수명: 7일)
            2.  발급된 Refresh Token은 서버의 안전한 저장소(Redis)에도 저장됩니다.
            
            **[클라이언트 처리]**
            1.  응답으로 받은 `accessToken`과 `refreshToken`을 **모두** 클라이언트의 안전한 공간에 저장해야 합니다.
            2.  이후 모든 API 요청에는 `Authorization` 헤더에 `accessToken`을 `Bearer` 형식으로 담아 보내야 합니다.
            """)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String userAgent = httpServletRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "토큰 재발급 (Token Rotation)",
            description = """
            ### **Access Token이 만료되었을 때, 이 API를 통해 새로운 토큰들을 발급받습니다.**
            
            **[호출 시점]**
            - 일반 API를 호출했는데 `401 Unauthorized` 에러를 응답받았을 때, 자동으로 이 API를 호출해야 합니다.
            
            **[요청 방법]**
            - `POST` 메서드로, Request Body에 이전에 받아 저장해 둔 `refreshToken`을 담아 요청합니다.
            
            **[서버 처리 및 응답]**
            - **성공 시 (토큰 로테이션)**: 서버는 보안을 위해 **완전히 새로운 Access Token과 Refresh Token을 모두** 재발급하여 응답합니다.
            - **실패 시**: Refresh Token이 만료되었거나, 이미 사용된 토큰(탈취 의심)일 경우 `401 Unauthorized` 에러를 반환합니다.
            
            **[클라이언트 처리]**
            1.  **재발급 성공 시**: 응답으로 받은 **새로운 `accessToken`과 `refreshToken`으로 기존 토큰들을 덮어쓰기 저장**해야 합니다. 그 후, 만료되어 실패했던 원래 API 요청을 새로운 `accessToken`으로 다시 시도합니다.
            2.  **재발급 실패 시**: 저장된 모든 토큰을 삭제하고, 사용자를 로그인 페이지로 보내 **강제 로그아웃** 시켜야 합니다.
            """)
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueTokenResponse>> reissueToken(@Valid @RequestBody ReissueTokenRequest request) {
        ReissueTokenResponse response = authService.reissueToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "로그아웃",
            description = """
            ### **현재 로그인된 사용자의 세션을 종료합니다.**
            
            **[인증]**
            - **필수**: `Authorization` 헤더에 유효한 Access Token을 포함해야 합니다.
            
            **[서버 처리]**
            - 서버에 저장된 사용자의 Refresh Token을 삭제하여, 현재 세션을 무효화시킵니다.
            
            **[클라이언트 처리]**
            - 이 API 호출 성공 시(`204 No Content` 응답), 클라이언트는 저장된 모든 토큰(Access, Refresh)을 삭제하고 사용자를 로그인 페이지로 리다이렉트해야 합니다.
            """)
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
    }

    @Operation(summary = "이메일 중복 확인",
            description = "회원가입 시 이메일 사용 가능 여부를 확인합니다. 사용 가능할 경우 `isAvailable` 필드가 `true`로 반환됩니다.")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }

    @Operation(summary = "닉네임 중복 확인",
            description = "회원가입 시 닉네임 사용 가능 여부를 확인합니다. 사용 가능할 경우 `isAvailable` 필드가 `true`로 반환됩니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }
}