package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.response.*;
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

@Tag(name = "Media Query Controller",
        description = "영화, TV 시리즈 등 다양한 미디어 콘텐츠를 탐색, 검색, 조회기능을 제공하는 API")
@Validated
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    // getMediaDetail, searchMedia, getChartSummary, getCuratedChart, getGenreChart, getPlatformChart 엔드포인트는 이전과 동일

    @Operation(summary = "미디어 상세 정보 조회",
            description = """
            ID를 기반으로 영화 또는 TV 시리즈의 상세 정보를 조회합니다.
            
            **응답에 포함되는 주요 정보**
            - **기본 정보**: 제목, 원제, 줄거리, 개봉일, 포스터 및 배경 이미지 경로
            - **평점 정보**: 평균 평점, 투표 수
            - **출연진 및 제작진**: 주요 출연진 (최대 10명), 감독 정보
            - **미디어 콘텐츠**: 예고편 및 관련 영상, 스틸컷 이미지 (최대 10개)
            - **관련 콘텐츠**: 추천 작품, 유사 작품 목록
            - **부가 정보**: 장르, 관련 키워드, 시청 가능한 OTT 플랫폼 (한국 기준)
            """)
    @GetMapping("/{mediaType}/{id}")
    public Mono<ApiResponse<MediaDetailDto>> getMediaDetail(
            @Parameter(description = "미디어 타입 (`movie` 또는 `tv`)", example = "movie") @PathVariable String mediaType,
            @Parameter(description = "TMDB의 고유 미디어 ID", example = "550") @PathVariable Long id) {
        return mediaQueryService.getMediaDetail(id, mediaType).map(ApiResponse::ok);
    }

    @Operation(summary = "미디어 통합 검색", description = "키워드로 영화와 TV 시리즈를 통합 검색합니다.")
    @GetMapping("/search")
    public Mono<ApiResponse<PagedResponse<MediaSummaryDto>>> searchMedia(
            @RequestParam @NotBlank String query, @RequestParam(defaultValue = "1") @Min(1) int page) {
        return mediaQueryService.searchMedia(query, page).map(ApiResponse::ok);
    }

    @Operation(summary = "메인 화면용 차트 요약 조회",
            description = """
            메인 화면에 표시될 여러 차트의 요약 목록을 한번에 조회합니다.
            이 API가 반환하는 차트의 종류와 순서는 고정되어 있지 않으며, 관리자 API를 통해 동적으로 변경될 수 있습니다.
            
            **차트 구성 변경은 `PUT /api/admin/settings/home-charts` 엔드포인트를 사용하세요.**
            """)
    @GetMapping("/charts/summary")
    public Mono<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        return mediaQueryService.getChartSummary().map(ApiResponse::ok);
    }

    @Operation(summary = "큐레이션 차트 조회", description = "서버에 미리 정의된 특별 큐레이션 차트를 페이지별로 조회합니다.")
    @GetMapping("/charts/{chartType}")
    public Mono<ApiResponse<PagedResponse<MediaChartDto>>> getCuratedChart(
            @Parameter(description = "조회할 차트의 종류", schema = @Schema(implementation = ChartType.class))
            @PathVariable String chartType,
            @RequestParam(defaultValue = "1") @Min(1) int page) {
        ChartType type = ChartType.fromString(chartType.toUpperCase());
        return mediaQueryService.getCuratedChart(type, page).map(ApiResponse::ok);
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
            
            ### 응답 데이터 사용법
            - **`label`**: UI에 사용자에게 보여줄 이름입니다. (예: "Netflix")
            - **`value`**: 실제 API 호출 시 경로 변수나 파라미터로 사용될 값입니다. (예: "NETFLIX")
            
            ### 이미지 경로(filePath) 사용법
            응답에 포함된 `filePath`는 부분 경로이므로, 전체 이미지 URL을 만들려면 앞에 TMDB 이미지 서버 주소를 붙여야 합니다.
            
            **공식: `[기본 URL]` + `[이미지 크기]` + `[파일 경로]`**
            
            - **기본 URL**: `https://image.tmdb.org/t/p/`
            - **이미지 크기**: `w92`, `w185`, `w300`, `original` 등 원하는 크기를 선택합니다. (권장: `w185`)
            - **파일 경로**: API 응답으로 받은 `filePath` 값 (예: `/t2yyOv4xD9xpcGPNavKrDdGFEly.jpg`)
            
            **최종 URL 예시:**
            `https://image.tmdb.org/t/p/w185/t2yyOv4xD9xpcGPNavKrDdGFEly.jpg`
            """)
    @GetMapping("/meta/filters")
    public Mono<ApiResponse<FilterMetadataResponse>> getFilterMetadata() {
        return mediaQueryService.getFilterMetadata().map(ApiResponse::ok);
    }
}