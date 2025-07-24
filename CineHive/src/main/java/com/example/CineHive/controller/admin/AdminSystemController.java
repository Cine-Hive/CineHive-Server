package com.example.CineHive.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 시스템 설정 API 컨트롤러입니다.
 */
@Tag(name = "Admin System Controller", description = "관리자용 시스템 설정 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    @Operation(summary = "전체 공지 발송")
    @PostMapping("/notifications")
    public void sendGlobalNotification() {
        // TODO: AdminSystemService.sendGlobalNotification(request) 호출
    }

    // --- 메타데이터 관리 ---
    @Operation(summary = "장르 메타데이터 목록 조회")
    @GetMapping("/meta/genres")
    public void getGenreMetadata() {
        // TODO: MetadataService.getAllGenresForAdmin() 호출
    }
    // ... (장르 POST, PUT 엔드포인트 스켈레톤)

    @Operation(summary = "플랫폼 메타데이터 목록 조회")
    @GetMapping("/meta/platforms")
    public void getPlatformMetadata() {
        // TODO: MetadataService.getAllPlatformsForAdmin() 호출
    }
    // ... (플랫폼 POST, PUT 엔드포인트 스켈레톤)

    // --- 기능 플래그 관리 ---
    @Operation(summary = "기능 플래그 목록 조회")
    @GetMapping("/feature-flags")
    public void getFeatureFlags() {
        // TODO: FeatureFlagService.getAllFlags() 호출
    }

    @Operation(summary = "기능 플래그 상태 변경")
    @PatchMapping("/feature-flags/{flagName}")
    public void updateFeatureFlag(@PathVariable String flagName) {
        // TODO: FeatureFlagService.updateFlag(flagName, request) 호출
    }
}