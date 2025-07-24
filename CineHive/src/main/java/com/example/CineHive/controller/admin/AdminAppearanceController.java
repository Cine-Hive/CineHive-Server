package com.example.CineHive.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 화면 노출(배너, 차트, 큐레이션) 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin Appearance Controller", description = "관리자용 화면 노출 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAppearanceController {

    // --- 배너 관리 ---
    @Operation(summary = "배너 목록 조회")
    @GetMapping("/banners")
    public void getAllBanners() {
        // TODO: 기존 AdminBannerController의 getAllBanners 로직
    }
    // ... (배너 POST, PUT, DELETE 엔드포인트 스켈레톤)

    // --- 홈 화면 차트 관리 ---
    @Operation(summary = "홈 화면 차트 설정 조회")
    @GetMapping("/home-charts")
    public void getHomeChartSettings() {
        // TODO: 기존 AdminHomeChartController의 getHomeChartSettings 로직
    }
    // ... (홈 차트 PUT, GET /available 엔드포인트 스켈레톤)

    // --- 큐레이션 관리 ---
    @Operation(summary = "큐레이션 목록 조회")
    @GetMapping("/curations")
    public void getCurationList() {
        // TODO: 1. AdminCurationService.getCurations() 호출
        // TODO: 2. CurationAdminResponse DTO 리스트로 반환
    }
    // ... (큐레이션 POST, PUT, DELETE 엔드포인트 스켈레톤)
}