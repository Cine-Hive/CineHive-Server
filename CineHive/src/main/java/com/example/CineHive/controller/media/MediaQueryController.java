package com.example.CineHive.controller.media;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.media.*;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Media Query Controller", description = "미디어 콘텐츠 탐색, 검색, 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    @Operation(summary = "미디어 상세 정보 조회")
    @GetMapping("/{mediaType}/{id}")
    public Mono<ResponseEntity<ApiResponse<MediaDetailResponse>>> getMediaDetail(
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)") @PathVariable String mediaType,
            @Parameter(description = "TMDB 미디어 ID") @PathVariable Long id) {

        return mediaQueryService.getMediaDetail(id, mediaType)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "미디어 통합 검색")
    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<PagedResponse<MediaSummaryResponse>>>> searchMedia(
            @RequestParam @NotBlank String query, @RequestParam(defaultValue = "1") @Min(1) int page) {

        return mediaQueryService.searchMedia(query, page)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "메인 화면용 차트 요약 조회")
    @GetMapping("/charts/summary")
    public Mono<ResponseEntity<ApiResponse<ChartSummaryResponse>>> getChartSummary() {
        return mediaQueryService.getChartSummary()
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "큐레이션 차트 조회")
    @GetMapping("/charts/{chartType}")
    public Mono<ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>>> getCuratedChart(
            @Parameter(description = "조회할 차트 종류", schema = @Schema(implementation = ChartType.class))
            @PathVariable String chartType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        return Mono.fromCallable(() -> ChartType.fromString(chartType.toUpperCase()))
                .flatMap(type -> mediaQueryService.getCuratedChart(type, page))
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "장르별 차트 조회")
    @GetMapping("/charts/genres/{genreId}")
    public Mono<ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>>> getGenreChart(
            @Parameter(description = "TMDB 장르 ID") @PathVariable Long genreId,
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)") @RequestParam(defaultValue = "movie") String mediaType,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") @Min(1) int page) {

        return mediaQueryService.getGenreChart(mediaType, genreId, page)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "플랫폼별 TV 시리즈 차트 조회")
    @GetMapping("/charts/platforms/{platform}")
    public Mono<ResponseEntity<ApiResponse<PagedResponse<MediaChartResponse>>>> getPlatformChart(
            @Parameter(description = "플랫폼 이름 (예: NETFLIX)", schema = @Schema(implementation = Platform.class))
            @PathVariable Platform platform,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "1") @Min(1) int page) {

        return mediaQueryService.getPlatformChart(platform, page)
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }

    @Operation(summary = "필터 메타데이터 조회")
    @GetMapping("/meta/filters")
    public Mono<ResponseEntity<ApiResponse<FilterMetadataResponse>>> getFilterMetadata() {
        return mediaQueryService.getFilterMetadata()
                .map(result -> ResponseEntity.ok(ApiResponse.ok(result)));
    }
}