package com.example.CineHive.controller.account;

import com.example.CineHive.dto.account.AccountInfoResponse;
import com.example.CineHive.dto.account.UpdateGenresRequest;
import com.example.CineHive.dto.account.UpdateNicknameRequest;
import com.example.CineHive.dto.account.UpdatePasswordRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증된 사용자의 계정 정보 관리 API 컨트롤러입니다.
 */
@Tag(name = "Account Controller", description = "인증된 사용자의 계정 정보 관리 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getMyInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        AccountInfoResponse accountInfo = accountService.getAccountInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(accountInfo));
    }

    @Operation(summary = "닉네임 변경", description = "현재 로그인된 사용자의 닉네임을 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검사 실패 (닉네임 규칙 위반)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<MessageResponse>> changeNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest request) {
        accountService.changeNickname(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("닉네임이 성공적으로 변경되었습니다.")));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검사 실패 (비밀번호 규칙 위반) 또는 기존 비밀번호 불일치", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<MessageResponse>> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        accountService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("비밀번호가 성공적으로 변경되었습니다.")));
    }

    @Operation(summary = "선호 장르 수정", description = "현재 로그인된 사용자의 선호 장르 목록을 수정합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"))
    @PutMapping("/me/genres")
    public ResponseEntity<ApiResponse<MessageResponse>> updateGenres(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateGenresRequest request) {
        accountService.updateGenres(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("선호 장르가 성공적으로 수정되었습니다.")));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제합니다. 모든 관련 데이터가 영구적으로 삭제되므로 주의가 필요합니다.")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"))
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        accountService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("회원 탈퇴가 성공적으로 처리되었습니다.")));
    }
}