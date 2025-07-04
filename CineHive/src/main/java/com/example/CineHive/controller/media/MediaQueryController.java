package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.response.*;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Media Query Controller", description = "CineHive 미디어 조회 API")
@Validated
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    @Operation(summary = "미디어 상세 정보 조회",
            description = "ID를 기반으로 영화 또는 TV 시리즈의 상세 정보를 조회합니다.")
    @GetMapping("/{mediaType}/{id}")
    public Mono<ApiResponse<MediaDetailDto>> getMediaDetail(
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)", example = "movie") @PathVariable String mediaType,
            @Parameter(description = "TMDB의 고유 미디어 ID", example = "550") @PathVariable Long id) {
        return mediaQueryService.getMediaDetail(id, mediaType)
                .map(ApiResponse::ok);
    }

    @Operation(summary = "미디어 통합 검색",
            description = "키워드로 영화와 TV 시리즈를 통합 검색합니다.")
    @GetMapping("/search")
    public Mono<ApiResponse<PagedResponse<MediaSummaryDto>>> searchMedia(
            @Parameter(description = "검색할 키워드", example = "어벤져스", required = true) @RequestParam @NotBlank String query,
            @Parameter(description = "검색 결과의 페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        return mediaQueryService.searchMedia(query, page)
                .map(ApiResponse::ok);
    }

    @Operation(summary = "메인 화면용 차트 요약 조회",
            description = "메인 화면에 표시될 여러 차트의 요약 목록을 한번에 조회합니다.")
    @GetMapping("/charts/summary")
    public Mono<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        return mediaQueryService.getChartSummary()
                .map(ApiResponse::ok);
    }

    @Operation(summary = "큐레이션 차트 조회",
            description = "서버에 미리 정의된 특별 큐레이션 차트를 페이지별로 조회합니다.")
    @GetMapping("/charts/{chartType}")
    public Mono<ApiResponse<PagedResponse<MediaChartDto>>> getCuratedChart(
            @Parameter(description = "조회할 차트의 종류",
                    schema = @Schema(implementation = ChartType.class))
            @PathVariable String chartType,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType type = ChartType.fromString(chartType.toUpperCase());
        return mediaQueryService.getCuratedChart(type, page)
                .map(ApiResponse::ok);
    }

    @Operation(summary = "장르별 차트 조회",
            description = "특정 장르의 영화/TV 시리즈 목록을 인기순으로 조회합니다. 사용 가능한 장르 ID 목록은 `/api/media/meta/filters` 엔드포인트를 통해 얻을 수 있습니다.")
    @GetMapping("/charts/genres/{genreId}")
    public Mono<ApiResponse<PagedResponse<MediaChartDto>>> getGenreChart(
            @Parameter(description = "TMDB 장르 ID", example = "28") @PathVariable Long genreId,
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)", example = "movie") @RequestParam(defaultValue = "movie") String mediaType,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        return mediaQueryService.getGenreChart(mediaType, genreId, page).map(ApiResponse::ok);
    }

    @Operation(summary = "플랫폼별 TV 시리즈 차트 조회",
            description = """
            특정 플랫폼(방송사)의 TV 시리즈 목록을 인기순으로 조회합니다.
            경로 변수에는 숫자 ID가 아닌, 플랫폼의 이름(Enum 상수)을 대문자로 사용합니다.
            
            **사용 가능한 플랫폼 이름 목록은 `/api/media/meta/filters` 엔드포인트를 통해 얻을 수 있습니다.**
            (예: `NETFLIX`, `DISNEY_PLUS`, `TVN` 등)
            """)
    @GetMapping("/charts/platforms/{platform}")
    public Mono<ApiResponse<PagedResponse<MediaChartDto>>> getPlatformChart(
            @Parameter(description = "조회할 플랫폼의 이름 (예: NETFLIX)",
                    schema = @Schema(implementation = Platform.class))
            @PathVariable Platform platform,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") @Min(1) int page) {
        return mediaQueryService.getPlatformChart(platform, page).map(ApiResponse::ok);
    }

    @Operation(summary = "필터 메타데이터 조회",
            description = """
            클라이언트에서 필터링 UI를 동적으로 구성하는 데 필요한 모든 데이터를 제공합니다.
            앱 실행 시 한 번 호출하여 캐시해두고 사용하는 것이 좋습니다.
            
            **응답 데이터 사용법:**
            - **`label`**: UI에 사용자에게 보여줄 이름입니다. (예: "Netflix")
            - **`value`**: 실제 API 호출 시 경로 변수나 파라미터로 사용될 값입니다. (예: "NETFLIX")
            """)
    @GetMapping("/meta/filters")
    public Mono<ApiResponse<FilterMetadataResponse>> getFilterMetadata() {
        return mediaQueryService.getFilterMetadata().map(ApiResponse::ok);
    }
}