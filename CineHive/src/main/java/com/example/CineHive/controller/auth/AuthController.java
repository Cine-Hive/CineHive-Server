package com.example.CineHive.controller.auth;

import com.example.CineHive.dto.auth.AvailabilityResponse;
import com.example.CineHive.dto.auth.LoginRequest;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.dto.auth.RegisterRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.service.auth.AuthService;
import com.example.CineHive.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // TODO: private final TokenService tokenService; // 토큰 재발급/로그아웃 로직을 위한 서비스 주입 필요

    @Operation(summary = "회원가입",
            description = "새로운 사용자를 시스템에 등록합니다. 성공 시 `201 CREATED` 상태 코드와 성공 메시지를 반환합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new MessageResponse("회원가입이 성공적으로 완료되었습니다.")));
    }

    @Operation(summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 JWT를 발급받습니다. `User-Agent` 헤더를 통해 로그인한 브라우저 정보를 기록합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String userAgent = httpServletRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "로그아웃",
            description = "서버에서 사용자의 리프레시 토큰을 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public void logout() {
        // TODO: 1. Request Header의 쿠키 또는 Authorization 헤더에서 Refresh Token 추출
        // TODO: 2. TokenService를 호출하여 저장된 Refresh Token 삭제
        // TODO: 3. 성공 시 MessageResponse 반환 (예: "로그아웃되었습니다.")
    }

    @Operation(summary = "액세스 토큰 재발급",
            description = "유효한 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    @PostMapping("/refresh")
    public void refresh() {
        // TODO: 1. Request Header의 쿠키 또는 Authorization 헤더에서 Refresh Token 추출
        // TODO: 2. TokenService를 호출하여 Refresh Token 유효성 검증
        // TODO: 3. 유효하다면 새로운 Access Token만 생성하여 응답 (신규 DTO: AccessTokenResponse 필요)
        // TODO: 4. 유효하지 않다면 401 Unauthorized 에러 응답
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