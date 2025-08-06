package com.example.CineHive.domain.admin;

import com.example.CineHive.domain.admin.dto.HomeChartSettingRequest;
import com.example.CineHive.domain.admin.dto.HomeChartSettingResponse;
import com.example.CineHive.domain.banner.dto.BannerAdminRequest;
import com.example.CineHive.domain.banner.dto.BannerAdminResponse;
import com.example.CineHive.global.dto.ApiResponse;
import com.example.CineHive.global.dto.MessageResponse;
import com.example.CineHive.domain.media.enums.ChartType;
import com.example.CineHive.domain.banner.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자용 화면 노출(배너, 차트, 큐레이션) 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin Appearance Controller", description = "관리자용 화면 노출 관리 API")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminAppearanceController {

    private final BannerService bannerService;
    private final AdminHomeChartService adminHomeChartService;
    // TODO: private final AdminCurationService adminCurationService;

    // =========================================
    // == 배너 관리 (Banner Management)
    // =========================================
    @Operation(summary = "배너 목록 조회", description = "활성화/비활성화 상태에 관계없이 모든 배너 목록을 조회합니다.")
    @GetMapping("/banners")
    public ResponseEntity<ApiResponse<List<BannerAdminResponse>>> getAllBanners() {
        List<BannerAdminResponse> banners = bannerService.findAllBannersForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }

    @Operation(summary = "새 배너 등록")
    @PostMapping("/banners")
    public ResponseEntity<ApiResponse<BannerAdminResponse>> createBanner(@Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse createdBanner = bannerService.createBanner(request);
        return ResponseEntity.ok(ApiResponse.ok(createdBanner));
    }

    @Operation(summary = "배너 정보 수정")
    @PutMapping("/banners/{bannerId}")
    public ResponseEntity<ApiResponse<BannerAdminResponse>> updateBanner(@PathVariable Long bannerId, @Valid @RequestBody BannerAdminRequest request) {
        BannerAdminResponse updatedBanner = bannerService.updateBanner(bannerId, request);
        return ResponseEntity.ok(ApiResponse.ok(updatedBanner));
    }

    @Operation(summary = "배너 삭제")
    @DeleteMapping("/banners/{bannerId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteBanner(@PathVariable Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("배너가 성공적으로 삭제되었습니다.")));
    }

    // =========================================
    // == 홈 화면 차트 관리 (Home Chart Management)
    // =========================================
    @Operation(summary = "홈 화면 차트 설정 조회", description = "현재 홈 화면에 표시되도록 설정된 차트 목록과 순서를 조회합니다.")
    @GetMapping("/home-charts")
    public ResponseEntity<ApiResponse<List<HomeChartSettingResponse>>> getHomeChartSettings() {
        List<HomeChartSettingResponse> settings = adminHomeChartService.getHomeChartSettings();
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @Operation(summary = "홈 화면 차트 설정 업데이트", description = "홈 화면에 표시될 차트 목록 전체를 새로 업데이트합니다.")
    @PutMapping("/home-charts")
    public ResponseEntity<ApiResponse<MessageResponse>> updateHomeChartSettings(
            @Valid @RequestBody List<HomeChartSettingRequest> settings) {
        adminHomeChartService.updateHomeChartSettings(settings);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("홈 화면 차트 설정이 성공적으로 업데이트되었습니다.")));
    }

    @Operation(summary = "선택 가능한 모든 차트 타입 조회", description = "관리자가 홈 화면 차트로 설정할 수 있는 모든 ChartType 목록을 제공합니다.")
    @GetMapping("/home-charts/available")
    public ResponseEntity<ApiResponse<List<ChartType>>> getAvailableChartTypes() {
        List<ChartType> chartTypes = adminHomeChartService.getAvailableChartTypes();
        return ResponseEntity.ok(ApiResponse.ok(chartTypes));
    }

    // =========================================
    // == 큐레이션 관리 (Curation Management)
    // =========================================
    @Operation(summary = "큐레이션 목록 조회")
    @GetMapping("/curations")
    public void getCurationList() {
        // TODO: AdminCurationService.getCurations() 호출
    }

    @Operation(summary = "새 큐레이션 생성")
    @PostMapping("/curations")
    public void createCuration() {
        // TODO: AdminCurationService.createCuration(request) 호출
    }

    @Operation(summary = "큐레이션 수정")
    @PutMapping("/curations/{curationId}")
    public void updateCuration(@PathVariable Long curationId) {
        // TODO: AdminCurationService.updateCuration(curationId, request) 호출
    }

    @Operation(summary = "큐레이션 삭제")
    @DeleteMapping("/curations/{curationId}")
    public void deleteCuration(@PathVariable Long curationId) {
        // TODO: AdminCurationService.deleteCuration(curationId) 호출
    }
}