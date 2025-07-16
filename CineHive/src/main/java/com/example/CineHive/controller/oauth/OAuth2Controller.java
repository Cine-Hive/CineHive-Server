package com.example.CineHive.controller.oauth;

import com.example.CineHive.dto.member.LoginResponseDto;
import com.example.CineHive.dto.oauth.AccessTokenRequest;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.entity.member.ProviderType;
import com.example.CineHive.service.oauth.OAuth2Service; // 구현체가 아닌 인터페이스를 import
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "OAuth2 Controller", description = "통합 소셜 로그인 API")
@Validated
@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {
    private final OAuth2Service oauth2Service;

    @Value("${naver.client.id}") private String naverClientId;
    @Value("${naver.redirect.uri}") private String naverRedirectUri;
    @Value("${kakao.client.id}") private String kakaoClientId;
    @Value("${kakao.redirect.uri}") private String kakaoRedirectUri;
    @Value("${google.client.id}") private String googleClientId;
    @Value("${google.redirect.uri}") private String googleRedirectUri;

    @Operation(summary = "소셜 로그인 페이지로 리다이렉트 (웹용)",
            description = "사용자를 각 소셜 로그인 플랫폼의 인증 페이지로 보냅니다. [사용법: 클라이언트는 이 API를 직접 호출하는 대신, 로그인 버튼에 `<a>` 태그를 사용하여 이 API의 URL로 링크를 걸어야 합니다.]")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "소셜 로그인 페이지로 리다이렉트 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 소셜 로그인 플랫폼", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{platform}")
    public void redirectToProvider(
            @Parameter(description = "소셜 로그인 플랫폼", example = "kakao", schema = @Schema(type = "string", allowableValues = {"naver", "kakao", "google"}))
            @PathVariable ProviderType platform, // 컨버터가 자동 변환
            HttpServletResponse response) throws IOException {

        String redirectUrl = switch (platform) {
            case NAVER -> "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" + naverClientId + "&redirect_uri=" + URLEncoder.encode(naverRedirectUri, StandardCharsets.UTF_8) + "&state=STATE_STRING";
            case KAKAO -> "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + kakaoClientId + "&redirect_uri=" + URLEncoder.encode(kakaoRedirectUri, StandardCharsets.UTF_8);
            case GOOGLE -> "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=" + googleClientId + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8) + "&scope=" + URLEncoder.encode("email profile", StandardCharsets.UTF_8);
            // LOCAL은 소셜 로그인이 아니므로 처리하지 않음
            case LOCAL -> throw new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + platform);
        };
        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "소셜 로그인 콜백 처리 (웹용)",
            description = "각 플랫폼으로부터 인가 코드를 받아 로그인을 처리하고 JWT를 발급합니다. [사용법: 이 API는 사용자가 직접 호출하는 것이 아니라, 소셜 플랫폼이 로그인 성공 후 사용자를 리다이렉션시키는 주소입니다.]")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인/회원가입 성공, JWT 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "외부 소셜 API 서버 통신 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/{platform}/callback")
    public ResponseEntity<ApiResponse<LoginResponseDto>> handleCallback(
            @Parameter(description = "소셜 로그인 플랫폼", example = "kakao")
            @PathVariable ProviderType platform,
            @Parameter(description = "플랫폼으로부터 발급받은 인가 코드")
            @RequestParam @NotBlank(message = "인가 코드는 필수입니다.") String code) {

        LoginResponseDto loginResponse = oauth2Service.loginWithCode(platform, code);
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }

    @Operation(summary = "소셜 로그인 (앱용)",
            description = "클라이언트 앱(iOS, Android)에서 SDK를 통해 직접 발급받은 액세스 토큰으로 로그인을 처리하고 JWT를 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인/회원가입 성공, JWT 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 액세스 토큰", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "외부 소셜 API 서버 통신 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/app/login/{platform}")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginFromApp(
            @Parameter(description = "소셜 로그인 플랫폼", example = "kakao")
            @PathVariable ProviderType platform,
            @Valid @RequestBody AccessTokenRequest request) {

        LoginResponseDto loginResponse = oauth2Service.loginWithAccessToken(platform, request.accessToken());
        return ResponseEntity.ok(ApiResponse.ok(loginResponse));
    }
}