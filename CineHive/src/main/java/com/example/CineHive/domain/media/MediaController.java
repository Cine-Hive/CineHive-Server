package com.example.CineHive.domain.media;

import com.example.CineHive.domain.media.dto.*;
import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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
    // TODO: private final CurationService curationService;

    // =========================================
    // == 미디어 정보 및 상태
    // =========================================

    @Operation(summary = "미디어 상세 정보 조회",
            description = """
            ID와 미디어 타입(`movie` 또는 `tv`)을 기반으로 상세 정보를 조회합니다.
            
            **응답에 포함되는 주요 정보:**
            - 기본 정보 (제목, 줄거리, 개봉일, 포스터 등)
            - 평점 및 인기도
            - 출연진 및 감독 (크레딧)
            - 관련 동영상 (예고편 등)
            - 추천 및 유사 작품 목록
            - 시청 가능 OTT 플랫폼 정보 (한국 기준)
            """)
    @GetMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<ApiResponse<MediaDetailResponse>> getMediaDetail(
            @PathVariable String mediaType, @PathVariable Long mediaId) {
        MediaDetailResponse result = mediaQueryService.getMediaDetail(mediaId, mediaType);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "미디어 고급 검색 (필터)",
            description = "장르, 플랫폼, 출시일 등 다양한 조건으로 미디어를 필터링하여 검색합니다.")
    @GetMapping("/search")
    public void searchMediaWithFilters(@Parameter(hidden = true) ChartProperties properties,
                                       @RequestParam(defaultValue = "1") @Min(1) int page) {
        // TODO: 1. ChartProperties DTO를 @ModelAttribute 또는 여러 @RequestParam으로 받습니다.
        // TODO: 2. mediaType 파라미터도 받아서 영화/TV 분기 처리가 필요합니다.
        // TODO: 3. MediaQueryService.discoverMedia(mediaType, properties, page) 호출
        // TODO: 4. PagedResponse<MediaSummaryResponse> 형태로 반환
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

    @Operation(summary = "메인 화면용 차트 요약 조회",
            description = "메인 화면에 표시될 여러 차트의 요약 목록을 한번에 조회합니다. 반환되는 차트의 종류와 순서는 관리자 API를 통해 동적으로 변경될 수 있습니다.")
    @GetMapping("/charts/summary")
    public ResponseEntity<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        ChartSummaryResponse result = mediaQueryService.getChartSummary();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "장르별 차트 조회",
            description = "특정 장르의 영화/TV 시리즈 목록을 인기순으로 조회합니다. 사용 가능한 장르 ID 목록은 `GET /api/v1/media/meta/filters` 엔드포인트를 통해 얻을 수 있습니다.")
    @GetMapping("/charts/genres/{genreId}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getGenreChart(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "movie") String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartResponse> result = mediaQueryService.getGenreChart(mediaType, genreId, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "플랫폼별 차트 조회",
            description = "특정 플랫폼의 TV 시리즈 목록을 인기순으로 조회합니다. 사용 가능한 플랫폼 ID 목록은 `GET /api/v1/meta/filters`를 통해 얻을 수 있습니다.")
    @GetMapping("/charts/platforms/{platformId}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getPlatformChart(
            @PathVariable Long platformId,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        Platform platform = Platform.fromId(platformId);
        PagedResponse<MediaChartResponse> result = mediaQueryService.getPlatformChart(platform, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // =========================================
    // == 트렌드 및 큐레이션
    // =========================================

    @Operation(summary = "인기 미디어 목록 조회",
            description = "현재 인기있는 영화 또는 TV 시리즈 목록을 조회합니다.")
    @GetMapping("/trends/popular")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getPopularMedia(
            @RequestParam(defaultValue = "movie") String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType chartType = "tv".equalsIgnoreCase(mediaType) ? ChartType.POPULAR_TV : ChartType.POPULAR_MOVIES;
        PagedResponse<MediaChartResponse> result = mediaQueryService.getCuratedChart(chartType, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "평점 높은 미디어 목록 조회",
            description = "평점이 높은 영화 또는 TV 시리즈 목록을 조회합니다.")
    @GetMapping("/trends/top-rated")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getTopRatedMedia(
            @RequestParam(defaultValue = "movie") String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType chartType = "tv".equalsIgnoreCase(mediaType) ? ChartType.TOP_RATED_TV : ChartType.TOP_RATED_MOVIES;
        PagedResponse<MediaChartResponse> result = mediaQueryService.getCuratedChart(chartType, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "현재 상영중인 영화 목록 조회")
    @GetMapping("/trends/now-playing")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getNowPlayingMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartResponse> result = mediaQueryService.getCuratedChart(ChartType.NOW_PLAYING_MOVIES, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "개봉 예정인 영화 목록 조회")
    @GetMapping("/trends/upcoming")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getUpcomingMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartResponse> result = mediaQueryService.getCuratedChart(ChartType.UPCOMING_MOVIES, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "주간 트렌드 미디어 목록 조회",
            description = "지난 주를 기준으로 가장 인기 있었던 영화 또는 TV 시리즈 목록을 조회합니다.")
    @GetMapping("/trends/weekly")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>> getWeeklyTrendingMedia(
            @RequestParam(defaultValue = "movie") String mediaType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType chartType = "tv".equalsIgnoreCase(mediaType) ? ChartType.TRENDING_TV_WEEK : ChartType.TRENDING_MOVIES_WEEK;
        PagedResponse<MediaChartResponse> result = mediaQueryService.getCuratedChart(chartType, page);
        return ResponseEntity.ok(ApiResponse.ok(result));
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