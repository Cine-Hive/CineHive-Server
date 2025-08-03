package com.example.CineHive.domain.auth.controller.entity;

import com.example.CineHive.domain.auth.dto.*;
import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.MessageResponse;
import com.example.CineHive.global.util.ClientIpExtractor;
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
 * 인증(회원가입, 로그인, 비밀번호 찾기) 및 토큰 관리 API 컨트롤러입니다.
 */
@Tag(name = "Auth Controller", description = "인증(회원가입, 로그인, 비밀번호 찾기) 및 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입",
            description = """
            ### **새로운 사용자를 시스템에 등록합니다.**
            
            **[주요 검증 규칙]**
            - 이메일, 닉네임은 시스템에서 유일해야 합니다.
            - 비밀번호는 보안 정책(영문, 숫자, 특수문자 조합 및 길이)을 준수해야 합니다.
            - 비밀번호와 비밀번호 확인 필드가 일치해야 합니다.
            
            **[응답]**
            - 성공 시 `201 CREATED` 상태 코드와 성공 메시지를 반환합니다.
            """)
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
                - **Refresh Token**: Access Token 재발급 시 사용 (수명: 30일)
            2.  발급된 Refresh Token은 서버의 안전한 저장소(Redis)에도 저장됩니다.
            
            **[계정 잠금 정책]**
            - 로그인에 **5회 연속 실패** 시, 해당 계정은 **15분 동안 잠금 처리**됩니다.
            - 잠긴 상태에서 로그인 시도 시 `423 Locked` 에러가 발생합니다.
            
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
        boolean isAvailable = authService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }

    @Operation(summary = "닉네임 중복 확인",
            description = "회원가입 시 닉네임 사용 가능 여부를 확인합니다. 사용 가능할 경우 `isAvailable` 필드가 `true`로 반환됩니다.")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = authService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }

    @Operation(summary = "비밀번호 찾기 요청 (재설정 이메일 발송)",
            description = """
            ### **사용자가 비밀번호를 잊었을 때 재설정 링크를 요청합니다.**
            
            **[요청]**
            - 사용자의 가입 이메일을 Request Body에 담아 요청합니다.
            
            **[서버 처리]**
            1.  요청된 이메일이 시스템에 존재하는지 확인합니다.
            2.  존재하는 경우, 30분간 유효한 일회용 비밀번호 재설정 토큰(`selector` + `validator`)을 생성합니다.
            3.  해당 토큰들이 포함된 링크를 사용자의 이메일로 발송합니다.
            
            **[요청 제한 (Rate Limit)]**
            - 동일 이메일 주소로는 **1분에 2번**까지 요청할 수 있습니다.
            - 동일 IP 주소에서는 **1시간에 20번**까지만 요청할 수 있습니다.
            - 제한 초과 시 `429 Too Many Requests` 에러가 발생합니다.
            
            **[응답]**
            - **이메일 존재 여부와 관계없이**, 보안을 위해 항상 `204 No Content`를 응답하여 이메일 주소의 유효성을 추측할 수 없도록 합니다.
            """)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpServletRequest) {
        String clientIp = ClientIpExtractor.getClientIp(httpServletRequest);
        authService.createPasswordResetToken(request, clientIp);
    }

    @Operation(summary = "비밀번호 재설정",
            description = """
            ### **이메일로 받은 토큰을 사용하여 비밀번호를 새로 설정합니다.**
            
            **[요청]**
            - 이메일 링크를 통해 받은 `selector`와 `validator`, 그리고 새로 설정할 `newPassword`, `confirmPassword`를 Request Body에 담아 요청합니다.
            
            **[서버 처리]**
            1.  전달된 `selector`와 `validator`가 유효한지(존재 여부, 해시 일치, 만료 여부) 검증합니다.
            2.  `newPassword`가 비밀번호 정책에 맞는지, `confirmPassword`와 일치하는지 검증합니다.
            3.  **최근에 사용했던 비밀번호는 재사용할 수 없습니다.**
            4.  모든 검증을 통과하면, 사용자의 비밀번호를 새로운 비밀번호로 업데이트합니다.
            5.  사용한 토큰은 즉시 무효화(삭제)됩니다.
            
            **[응답]**
            - 성공 시, "비밀번호가 성공적으로 재설정되었습니다." 메시지를 반환합니다.
            - 토큰이 유효하지 않거나 만료된 경우 `400 Bad Request` 에러를 반환합니다.
            """)
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<MessageResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("비밀번호가 성공적으로 재설정되었습니다.")));
    }
}