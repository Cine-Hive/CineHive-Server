package com.example.CineHive.controller.auth;

import com.example.CineHive.dto.auth.*;
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
            description = "이메일과 비밀번호로 로그인하고 Access/Refresh Token을 발급받습니다. `User-Agent` 헤더를 통해 로그인한 브라우저 정보를 기록합니다.")
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
            ### Access Token이 만료되었을 때, 유효한 Refresh Token을 사용하여 새로운 토큰들을 발급받습니다.
            - **요청**: `application/json` 형식으로 Request Body에 Refresh Token을 담아 전송해야 합니다.
            - **성공**: 새로운 Access Token과 Refresh Token이 함께 반환됩니다. 클라이언트는 이 새로운 토큰들로 교체하여 저장해야 합니다.
            - **실패**: Refresh Token이 유효하지 않거나 탈취가 의심될 경우 `401 Unauthorized` 에러가 발생하며, 이 경우 사용자는 다시 로그인해야 합니다.
            """)
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueTokenResponse>> reissueToken(@Valid @RequestBody ReissueTokenRequest request) {
        ReissueTokenResponse response = authService.reissueToken(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "로그아웃",
            description = """
            ### 현재 로그인된 사용자를 로그아웃 처리합니다.
            - **요청**: `Authorization` 헤더에 유효한 Access Token을 포함하여 요청해야 합니다.
            - **처리**: 서버는 해당 사용자의 Refresh Token을 저장소(Redis)에서 삭제하여, 더 이상 토큰 재발급이 불가능하도록 만듭니다.
            - **클라이언트**: 이 API 호출 성공 후, 클라이언트 측에 저장된 모든 토큰(Access, Refresh)을 삭제해야 합니다.
            """)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<MessageResponse>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("성공적으로 로그아웃되었습니다.")));
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
