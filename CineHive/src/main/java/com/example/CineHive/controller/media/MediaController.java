package com.example.CineHive.controller.media;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.media.*;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 미디어 정보 조회, 차트, 트렌드, 큐레이션 등 미디어 탐색 기능을 총괄하는 API 컨트롤러입니다.
 */
@Tag(name = "Media Controller", description = "미디어 탐색 및 상호작용 API")
@Validated
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaQueryService mediaQueryService;
    // TODO: private final UserMediaStatusService userMediaStatusService;

    // =========================================
    // == 미디어 정보 및 상태
    // =========================================

    @Operation(summary = "미디어 상세 정보 조회")
    @GetMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<ApiResponse<MediaDetailResponse>> getMediaDetail(
            @PathVariable String mediaType, @PathVariable Long mediaId) {
        MediaDetailResponse result = mediaQueryService.getMediaDetail(mediaId, mediaType).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "미디어 고급 검색 (필터)",
            description = "장르, 플랫폼, 출시일 등 다양한 조건으로 미디어를 필터링하여 검색합니다.")
    @GetMapping("/search")
    public void searchMediaWithFilters() {
        // TODO: 1. @RequestParam으로 다양한 필터 조건(ChartProperties)을 받음
        // TODO: 2. MediaQueryService.discoverMedia 호출
        // TODO: 3. PagedResponse<MediaSummaryResponse> 형태로 반환
    }

    @Operation(summary = "내 미디어 시청 상태 변경",
            description = "'보고싶어요', '보는중', '다봤어요' 등 특정 미디어에 대한 나의 시청 상태를 기록/수정합니다.")
    @PutMapping("/{mediaType}/{mediaId}/my-status")
    public void updateMyMediaStatus(
            @PathVariable String mediaType, @PathVariable Long mediaId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. UpdateMediaStatusRequest (신규 DTO)를 @RequestBody로 받음
        // TODO: 2. UserMediaStatusService.updateStatus(userEmail, mediaId, mediaType, request) 호출
        // TODO: 3. 성공 시 MessageResponse 반환
    }

    // =========================================
    // == 차트 및 요약
    // =========================================

    @Operation(summary = "메인 화면용 차트 요약 조회")
    @GetMapping("/charts/summary")
    public ResponseEntity<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        ChartSummaryResponse result = mediaQueryService.getChartSummary().block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "장르별 차트 조회")
    @GetMapping("/charts/genres/{genreId}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getGenreChart(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "movie") String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartResponse> result = mediaQueryService.getGenreChart(mediaType, genreId, page).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "플랫폼별 차트 조회")
    @GetMapping("/charts/platforms/{platformId}")
    public void getPlatformChart(@PathVariable Long platformId) {
        // TODO: 1. MediaQueryService.getPlatformChart(platformId, page) 호출
        // TODO: 2. PagedResponse<MediaChartResponse> 형태로 반환
    }

    // =========================================
    // == 트렌드 및 큐레이션
    // =========================================

    @Operation(summary = "인기 미디어 목록 조회")
    @GetMapping("/trends/popular")
    public void getPopularMedia() {
        // TODO: 1. MediaQueryService.getCuratedChart(ChartType.POPULAR_MOVIES, page) 또는 POPULAR_TV 호출
        // TODO: 2. 미디어 타입("movie" or "tv")을 @RequestParam으로 받아 분기 처리
        // TODO: 3. PagedResponse<MediaChartResponse> 형태로 반환
    }

    @Operation(summary = "평점 높은 미디어 목록 조회")
    @GetMapping("/trends/top-rated")
    public void getTopRatedMedia() {
        // TODO: MediaQueryService.getCuratedChart(ChartType.TOP_RATED_MOVIES, page) ...
    }

    @Operation(summary = "현재 상영중인 영화 목록 조회")
    @GetMapping("/trends/now-playing")
    public void getNowPlayingMovies() {
        // TODO: MediaQueryService.getCuratedChart(ChartType.NOW_PLAYING_MOVIES, page) ...
    }

    @Operation(summary = "개봉 예정인 영화 목록 조회")
    @GetMapping("/trends/upcoming")
    public void getUpcomingMovies() {
        // TODO: MediaQueryService.getCuratedChart(ChartType.UPCOMING_MOVIES, page) ...
    }

    @Operation(summary = "주간 트렌드 미디어 목록 조회")
    @GetMapping("/trends/weekly")
    public void getWeeklyTrendingMedia() {
        // TODO: MediaQueryService.getCuratedChart(ChartType.TRENDING_MOVIES_WEEK, page) ...
    }

    @Operation(summary = "큐레이션 목록 조회",
            description = "관리자가 생성한 모든 큐레이션(테마별 추천) 목록을 조회합니다.")
    @GetMapping("/curations")
    public void getCurationList() {
        // TODO: 1. CurationService에서 큐레이션 목록 조회
        // TODO: 2. CurationSummaryResponse (신규 DTO) 리스트로 변환하여 반환
    }

    @Operation(summary = "특정 큐레이션 상세 조회",
            description = "특정 큐레이션에 포함된 미디어 목록 등 상세 정보를 조회합니다.")
    @GetMapping("/curations/{curationId}")
    public void getCurationDetail(@PathVariable Long curationId) {
        // TODO: 1. CurationService에서 curationId로 상세 정보 조회
        // TODO: 2. CurationDetailResponse (신규 DTO)로 변환하여 반환
    }
}