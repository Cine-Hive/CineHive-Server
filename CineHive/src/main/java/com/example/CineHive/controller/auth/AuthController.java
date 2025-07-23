package com.example.CineHive.controller.auth; // 패키지 이동

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

@Tag(name = "Auth Controller", description = "인증(회원가입, 로그인) 및 중복 확인 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MessageResponse>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new MessageResponse("회원가입이 성공적으로 완료되었습니다.")));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {

        String userAgent = httpServletRequest.getHeader("User-Agent");
        LoginResponse response = authService.login(request, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmail(@RequestParam String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.ok(new AvailabilityResponse(isAvailable)));
    }
}