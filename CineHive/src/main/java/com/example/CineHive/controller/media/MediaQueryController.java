package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.response.*;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "", description = "영화, TV 시리즈 등 다양한 미디어 콘텐츠를 탐색, 검색, 조회하기 위한 핵심 API입니다.")
@Validated
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    @Operation(summary = "미디어 상세 정보 조회",
            description = "ID를 기반으로 영화 또는 TV 시리즈의 상세 정보를 조회합니다. 응답에 포함되는 주요 정보는 기본 정보, 평점, 출연진, 관련 영상, 추천 작품 등입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 미디어 타입 (movie, tv만 가능)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 ID의 미디어를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{mediaType}/{id}")
    public ResponseEntity<ApiResponse<MediaDetailDto>> getMediaDetail(
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)", example = "movie") @PathVariable String mediaType,
            @Parameter(description = "TMDB의 고유 미디어 ID", example = "550") @PathVariable Long id) {
        MediaDetailDto result = mediaQueryService.getMediaDetail(id, mediaType).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "미디어 통합 검색", description = "키워드로 영화와 TV 시리즈를 통합 검색합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검색어(query) 파라미터 누락", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<MediaSummaryDto>>> searchMedia(
            @RequestParam @NotBlank String query, @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaSummaryDto> result = mediaQueryService.searchMedia(query, page).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "메인 화면용 차트 요약 조회",
            description = "메인 화면에 표시될 여러 차트의 요약 목록을 한번에 조회합니다. 반환되는 차트의 종류와 순서는 관리자 API(`PUT /api/v1/admin/home-charts`)를 통해 동적으로 변경될 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류 (일부 차트 조회 실패 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/charts/summary")
    public ResponseEntity<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        ChartSummaryResponse result = mediaQueryService.getChartSummary().block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "큐레이션 차트 조회", description = "서버에 미리 정의된 특별 큐레이션 차트를 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 차트 타입", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/charts/{chartType}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getCuratedChart(
            @Parameter(description = "조회할 차트의 종류", schema = @Schema(implementation = ChartType.class))
            @PathVariable String chartType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType type = ChartType.fromString(chartType.toUpperCase());
        PagedResponse<MediaChartDto> result = mediaQueryService.getCuratedChart(type, page).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "장르별 차트 조회",
            description = "특정 장르의 영화/TV 시리즈 목록을 인기순으로 조회합니다. 사용 가능한 장르 ID 목록은 `/api/v1/media/meta/filters` 엔드포인트를 통해 얻을 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/charts/genres/{genreId}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getGenreChart(
            @Parameter(description = "TMDB 장르 ID", example = "28") @PathVariable Long genreId,
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)", example = "movie") @RequestParam(defaultValue = "movie") String mediaType,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartDto> result = mediaQueryService.getGenreChart(mediaType, genreId, page).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "플랫폼별 TV 시리즈 차트 조회",
            description = "특정 플랫폼의 TV 시리즈 목록을 인기순으로 조회합니다. 사용 가능한 플랫폼 이름 목록은 `/api/v1/media/meta/filters`를 통해 얻을 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 플랫폼 이름", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/charts/platforms/{platform}")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getPlatformChart(
            @Parameter(description = "조회할 플랫폼의 이름 (예: NETFLIX)", schema = @Schema(implementation = Platform.class))
            @PathVariable Platform platform,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        PagedResponse<MediaChartDto> result = mediaQueryService.getPlatformChart(platform, page).block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "필터 메타데이터 조회",
            description = "클라이언트에서 필터링 UI를 동적으로 구성하는 데 필요한 모든 데이터를 제공합니다. (장르, 플랫폼, 정렬 옵션 등)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류 (TMDB API 통신 실패 등)", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/meta/filters")
    public ResponseEntity<ApiResponse<FilterMetadataResponse>> getFilterMetadata() {
        FilterMetadataResponse result = mediaQueryService.getFilterMetadata().block();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}