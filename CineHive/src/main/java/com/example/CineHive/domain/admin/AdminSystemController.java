package com.example.CineHive.domain.admin;

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

    // TODO: private final AdminSystemService adminSystemService;
    // TODO: private final MetadataService metadataService;
    // TODO: private final FeatureFlagService featureFlagService;

    @Operation(summary = "전체 공지 발송")
    @PostMapping("/notifications")
    public void sendGlobalNotification() {
        // TODO: 1. NotificationRequest DTO를 @RequestBody로 받음
        // TODO: 2. AdminSystemService.sendGlobalNotification(request) 호출
        // TODO: 3. 성공 시 MessageResponse 반환
    }

    // =========================================
    // == 메타데이터 관리
    // =========================================

    @Operation(summary = "장르 메타데이터 목록 조회 (관리자용)")
    @GetMapping("/meta/genres")
    public void getGenreMetadata() {
        // TODO: MetadataService.getAllGenresForAdmin() 호출
    }

    @Operation(summary = "장르 정보 추가")
    @PostMapping("/meta/genres")
    public void addGenre() {
        // TODO: 1. CreateGenreRequest DTO를 @RequestBody로 받음
        // TODO: 2. MetadataService.addGenre(request) 호출
    }

    @Operation(summary = "장르 정보 수정")
    @PutMapping("/meta/genres/{genreId}")
    public void updateGenre(@PathVariable Long genreId) {
        // TODO: 1. UpdateGenreRequest DTO를 @RequestBody로 받음
        // TODO: 2. MetadataService.updateGenre(genreId, request) 호출
    }

    @Operation(summary = "플랫폼 메타데이터 목록 조회 (관리자용)")
    @GetMapping("/meta/platforms")
    public void getPlatformMetadata() {
        // TODO: MetadataService.getAllPlatformsForAdmin() 호출
    }

    @Operation(summary = "플랫폼 정보 추가")
    @PostMapping("/meta/platforms")
    public void addPlatform() {
        // TODO: 1. CreatePlatformRequest DTO를 @RequestBody로 받음
        // TODO: 2. MetadataService.addPlatform(request) 호출
    }

    @Operation(summary = "플랫폼 정보 수정")
    @PutMapping("/meta/platforms/{platformId}")
    public void updatePlatform(@PathVariable Long platformId) {
        // TODO: 1. UpdatePlatformRequest DTO를 @RequestBody로 받음
        // TODO: 2. MetadataService.updatePlatform(platformId, request) 호출
    }

    // =========================================
    // == 기능 플래그 관리
    // =========================================

    @Operation(summary = "기능 플래그 목록 조회")
    @GetMapping("/feature-flags")
    public void getFeatureFlags() {
        // TODO: FeatureFlagService.getAllFlags() 호출
    }

    @Operation(summary = "기능 플래그 상태 변경")
    @PatchMapping("/feature-flags/{flagName}")
    public void updateFeatureFlag(@PathVariable String flagName) {
        // TODO: 1. UpdateFeatureFlagRequest DTO (enabled: boolean)를 @RequestBody로 받음
        // TODO: 2. FeatureFlagService.updateFlag(flagName, request) 호출
    }
}