package com.example.CineHive.controller.admin;

import com.example.CineHive.entity.banner.Banner;
import com.example.CineHive.dto.banner.BannerAdminRequestDto;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.dto.response.ErrorResponse;
import com.example.CineHive.service.banner.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Banner Controller", description = "관리자용 배너 관리(CRUD) API")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class BannerAdminController {

    private final BannerService bannerService;

    @Operation(summary = "모든 배너 목록 조회 (관리자용)",
            description = "활성화/비활성화 상태에 관계없이 모든 배너 목록을 조회합니다. 관리자 페이지에서 배너를 관리할 때 사용합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 유효하지 않은 토큰)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 역할 아님)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Banner>>> getAllBanners() {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.findAllBannersForAdmin()));
    }

    @Operation(summary = "새 배너 생성",
            description = "새로운 배너를 생성하여 데이터베이스에 저장합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 유효성 검사 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Banner>> createBanner(@Valid @RequestBody BannerAdminRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.createBanner(requestDto)));
    }

    @Operation(summary = "기존 배너 수정",
            description = "특정 ID를 가진 배너의 모든 정보를 업데이트합니다. 배너를 비활성화하려면 `isActive` 값을 `false`로 설정하여 요청합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 또는 유효성 검사 실패)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 배너 ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Banner>> updateBanner(@PathVariable Long bannerId, @Valid @RequestBody BannerAdminRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(bannerService.updateBanner(bannerId, requestDto)));
    }

    @Operation(summary = "배너 삭제",
            description = "특정 ID를 가진 배너를 데이터베이스에서 영구적으로 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 배너 ID", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}