package com.example.CineHive.controller.member;

import com.example.CineHive.dto.auth.LoginRequest;
import com.example.CineHive.dto.auth.RegisterRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.dto.auth.LoginResponse;
import com.example.CineHive.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Member Controller", description = "회원 인증 및 관리 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "새로운 회원을 시스템에 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 또는 중복된 이메일/닉네임", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest requestDto) {
        memberService.register(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다.")));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT를 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 이메일 또는 비밀번호", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest requestDto,
            HttpServletRequest request) { // User-Agent 추출을 위해 request 사용

        String userAgent = request.getHeader("User-Agent");
        LoginResponse response = memberService.login(requestDto, userAgent);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "이메일 중복 확인", description = "회원가입 시 이메일 사용 가능 여부를 확인합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"))
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        boolean isAvailable = memberService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isAvailable", isAvailable)));
    }

    @Operation(summary = "닉네임 중복 확인", description = "회원가입 시 닉네임 사용 가능 여부를 확인합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "확인 성공"))
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkNickname(@RequestParam String nickname) {
        boolean isAvailable = memberService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isAvailable", isAvailable)));
    }
}