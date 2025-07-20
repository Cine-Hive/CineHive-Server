package com.example.CineHive.controller.account;

import com.example.CineHive.dto.account.*;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Account Controller", description = "인증된 사용자의 계정 정보 관리 API")
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        AccountInfoResponse accountInfo = accountService.getAccountInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(accountInfo));
    }

    @Operation(summary = "닉네임 변경", description = "현재 로그인된 사용자의 닉네임을 변경합니다.")
    @PatchMapping("/nickname")
    public ResponseEntity<ApiResponse<Map<String, String>>> changeNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest requestDto) {
        accountService.changeNickname(userDetails.getUsername(), requestDto.nickname());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "닉네임이 성공적으로 변경되었습니다.")));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest requestDto) {
        accountService.changePassword(userDetails.getUsername(), requestDto.oldPassword(), requestDto.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다.")));
    }

    @Operation(summary = "선호 장르 수정", description = "현재 로그인된 사용자의 선호 장르 목록을 수정합니다.")
    @PutMapping("/genres")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateGenres(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateGenresRequest requestDto) {
        accountService.updateGenres(userDetails.getUsername(), requestDto.genres());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "선호 장르가 성공적으로 수정되었습니다.")));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제합니다. 모든 관련 데이터가 영구적으로 삭제됩니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        accountService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다.")));
    }
}