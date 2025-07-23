package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.banner.BannerAdminRequest;
import com.example.CineHive.dto.banner.BannerAdminResponse;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.service.banner.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Banner Controller", description = "관리자용 배너 관리 API")
@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "모든 배너 목록 조회 (관리자용)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BannerAdminResponse>>> getAllBanners() {
        // 반환 타입을 DTO로 변경
        List<BannerAdminResponse> banners = bannerService.findAllBannersForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }

    @Operation(summary = "새 배너 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<BannerAdminResponse>> createBanner(@Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse createdBanner = bannerService.createBanner(request);
        return ResponseEntity.ok(ApiResponse.ok(createdBanner));
    }

    @Operation(summary = "기존 배너 수정")
    @PutMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<BannerAdminResponse>> updateBanner(
            @PathVariable Long bannerId,
            @Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse updatedBanner = bannerService.updateBanner(bannerId, request);
        return ResponseEntity.ok(ApiResponse.ok(updatedBanner));
    }

    @Operation(summary = "배너 삭제")
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteBanner(@PathVariable Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("배너가 성공적으로 삭제되었습니다.")));
    }
}