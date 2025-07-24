package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.banner.BannerAdminRequest;
import com.example.CineHive.dto.banner.BannerAdminResponse;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.ErrorResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.service.banner.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 배너 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin Banner Controller", description = "관리자용 배너 관리 API")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "모든 배너 목록 조회 (관리자용)",
            description = "활성화/비활성화 상태에 관계없이 모든 배너 목록을 조회합니다. 관리 페이지에서 사용됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 역할 필요)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerAdminResponse>>> getAllBanners() {
        List<BannerAdminResponse> banners = bannerService.findAllBannersForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }

    @Operation(summary = "새 배너 생성",
            description = "새로운 배너를 생성하여 데이터베이스에 저장합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BannerAdminResponse>> createBanner(@Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse createdBanner = bannerService.createBanner(request);
        return ResponseEntity.ok(ApiResponse.ok(createdBanner));
    }

    @Operation(summary = "기존 배너 수정",
            description = "특정 ID를 가진 배너의 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<BannerAdminResponse>> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse updatedBanner = bannerService.updateBanner(bannerId, request);
        return ResponseEntity.ok(ApiResponse.ok(updatedBanner));
    }

    @Operation(summary = "배너 삭제",
            description = "특정 ID를 가진 배너를 데이터베이스에서 영구적으로 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "배너를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteBanner(@PathVariable Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("배너가 성공적으로 삭제되었습니다.")));
    }
}