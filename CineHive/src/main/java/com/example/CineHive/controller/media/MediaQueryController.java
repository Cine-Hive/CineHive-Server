package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.response.*;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import com.example.CineHive.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Media Query Controller", description = "CineHive 미디어 조회 API - 영화 및 TV 시리즈 정보 검색, 차트 조회, 상세 정보 제공")
@Validated
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    @Operation(
            summary = "미디어 상세 정보 조회",
            description = """
            TMDB ID를 기반으로 영화 또는 TV 시리즈의 상세 정보를 조회합니다.
            
            **제공되는 정보:**
            - 기본 정보: 제목, 줄거리, 평점, 포스터 등
            - 장르, 출연진 (상위 10명), 감독/제작진 정보
            - 트레일러 및 관련 영상
            - 포스터 및 배경 이미지 (최대 10개)
            - 추천 작품 및 유사 작품 (각각 최대 10개)
            - 키워드 정보
            - 스트리밍 서비스 제공 정보 (한국/미국 기준)
            
            **애니메이션 식별:**
            - 장르 ID 16번(애니메이션)이 포함된 경우 `isAnimation: true`로 표시
            
            **언어 설정:**
            - 기본 언어: 한국어 (ko-KR)
            - 이미지는 한국어 및 언어 무관 이미지 포함
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상세 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 미디어 타입 또는 ID)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "message": "유효하지 않은 미디어 타입입니다. 'movie' 또는 'tv'만 지원됩니다.",
                        "data": null
                    }
                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "미디어를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "message": "해당 ID의 영화를 찾을 수 없습니다.",
                        "data": null
                    }
                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    @GetMapping("/{mediaType}/{id}")
    public Mono<com.example.CineHive.dto.response.ApiResponse<MediaDetailDto>> getMediaDetail(
            @Parameter(
                    description = """
                    미디어 타입을 지정합니다.
                    - `movie`: 영화
                    - `tv`: TV 시리즈/드라마
                    """,
                    example = "movie",
                    required = true
            )
            @PathVariable String mediaType,
            @Parameter(
                    description = """
                    TMDB(The Movie Database)의 고유 미디어 ID입니다.
                    
                    **예시:**
                    - 550: 파이트 클럽 (Fight Club)
                    - 238: 대부 (The Godfather)
                    - 1399: 왕좌의 게임 (Game of Thrones)
                    """,
                    example = "550",
                    required = true
            )
            @PathVariable Long id) {

        return mediaQueryService.getMediaDetail(id, mediaType)
                .map(com.example.CineHive.dto.response.ApiResponse::ok);
    }

    @Operation(
            summary = "미디어 차트 조회",
            description = """
            다양한 종류의 미디어 차트를 페이지별로 조회합니다.
            
            **지원하는 차트 타입:**
            
            **📽️ 영화 차트:**
            - `POPULAR_MOVIES`: 인기 영화 (현재 화제의 영화)
            - `TOP_RATED_MOVIES`: 평점 높은 영화 (IMDb 평점 기준)
            - `NOW_PLAYING_MOVIES`: 현재 상영중 영화
            - `UPCOMING_MOVIES`: 개봉 예정 영화
            
            **📺 TV 시리즈 차트:**
            - `POPULAR_TV`: 인기 TV 시리즈
            - `TOP_RATED_TV`: 평점 높은 TV 시리즈
            - `ON_THE_AIR_TV`: 현재 방영중 TV 시리즈
            - `AIRING_TODAY_TV`: 오늘 방영 TV 시리즈
            
            **🎨 애니메이션 전용 차트:**
            - `POPULAR_ANIMATION_MOVIES`: 인기 애니메이션 영화
            - `TOP_RATED_ANIMATION_MOVIES`: 평점 높은 애니메이션 영화
            - `NOW_PLAYING_ANIMATION_MOVIES`: 현재 상영중 애니메이션 영화
            - `UPCOMING_ANIMATION_MOVIES`: 개봉 예정 애니메이션 영화
            - `POPULAR_ANIMATION_TV`: 인기 애니메이션 TV 시리즈
            - `TOP_RATED_ANIMATION_TV`: 평점 높은 애니메이션 TV 시리즈
            - `ON_THE_AIR_ANIMATION_TV`: 현재 방영중 애니메이션 TV 시리즈
            - `UPCOMING_ANIMATION_TV`: 방영 예정 애니메이션 TV 시리즈
            
            **🏆 차트 정보:**
            - 각 항목에는 순위(rank)가 자동으로 할당됩니다
            - 애니메이션 작품은 `isAnimation: true`로 표시됩니다
            - 실시간 TMDB 데이터를 기반으로 업데이트됩니다
            
            **📄 페이징:**
            - 페이지 크기: 20개
            - 페이지 번호는 1부터 시작합니다
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "차트 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 차트 타입 또는 페이지 파라미터)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "message": "유효하지 않은 차트 타입입니다.",
                        "data": null
                    }
                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 TMDB API 오류"
            )
    })
    @GetMapping("/charts/{chartType}")
    public Mono<ApiResponse<PagedResponse<MediaChartDto>>> getChart(
            @Parameter(
                    description = "조회할 차트의 종류를 선택합니다.",
                    required = true,
                    // schema 속성을 사용하여 Enum 값을 명시적으로 지정
                    schema = @Schema(type = "string", implementation = ChartType.class)
            )
            @PathVariable String chartType,
            @Parameter(description = "조회할 페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        ChartType type = ChartType.fromString(chartType.toUpperCase());
        return mediaQueryService.getChart(type, page)
                .map(ApiResponse::ok);
    }

    @Operation(
            summary = "미디어 통합 검색",
            description = """
            키워드를 사용하여 영화와 TV 시리즈를 통합 검색합니다.
            
            **🔍 검색 특징:**
            - 영화와 TV 시리즈를 동시에 검색합니다
            - 제목, 원제, 줄거리 등에서 키워드를 찾습니다
            - 한국어 및 영어 검색을 모두 지원합니다
            - 부분 일치 검색이 가능합니다
            
            **📝 검색 팁:**
            - 영어 제목과 한국어 제목 모두 시도해보세요
            - 띄어쓰기는 자동으로 처리됩니다
            - 특수문자는 제거하고 검색하는 것을 권장합니다
            
            **🎯 결과 정보:**
            - 각 결과에는 미디어 타입(영화/TV)이 포함됩니다
            - 애니메이션 작품은 `isAnimation: true`로 표시됩니다
            - 평점과 포스터 이미지 정보가 포함됩니다
            
            **📊 정렬 기준:**
            - TMDB의 관련성(relevance) 점수 기준으로 정렬됩니다
            - 인기도와 평점이 함께 고려됩니다
            
            **💡 사용 예시:**
            - "아바타": 영화 "아바타" 및 TV 시리즈 "아바타 아바타" 등
            - "marvel": 마블 관련 모든 영화와 TV 시리즈
            - "기생충": 봉준호 감독의 영화 "기생충"
            - "스파이더맨": 모든 스파이더맨 관련 작품들
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검색 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (검색어 누락 또는 유효하지 않은 페이지 파라미터)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": false,
                        "message": "검색어는 필수입니다.",
                        "data": null
                    }
                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 TMDB API 오류"
            )
    })
    @GetMapping("/search")
    public Mono<com.example.CineHive.dto.response.ApiResponse<PagedResponse<MediaSummaryDto>>> searchMedia(
            @Parameter(
                    description = """
                    검색할 키워드입니다.
                    
                    **검색어 규칙:**
                    - 최소 1글자 이상 입력해야 합니다
                    - 공백만으로는 검색할 수 없습니다
                    - 특수문자도 검색 가능하지만 결과가 제한적일 수 있습니다
                    
                    **검색어 예시:**
                    - "어벤져스" (한국어 제목)
                    - "Avengers" (영어 제목)  
                    - "iron man" (띄어쓰기 포함)
                    - "naruto" (애니메이션)
                    - "game of thrones" (TV 시리즈)
                    """,
                    example = "어벤져스",
                    required = true
            )
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String query,
            @Parameter(
                    description = """
                    검색 결과의 페이지 번호입니다.
                    
                    - 최소값: 1
                    - 검색 결과가 많을 경우 페이지로 나누어 제공됩니다
                    """,
                    example = "1"
            )
            @RequestParam(defaultValue = "1") @Min(1) int page) {

        return mediaQueryService.searchMedia(query, page)
                .map(com.example.CineHive.dto.response.ApiResponse::ok);
    }

    @Operation(
            summary = "메인 화면용 차트 요약 조회",
            description = """
            메인 화면에서 사용할 여러 종류의 차트 상위 항목들을 한 번에 조회합니다.
            
            **🏠 메인 화면 최적화**
            - 각 차트에서 상위 10개 항목만 제공하여 빠른 로딩을 보장합니다
            - 모든 API 호출이 병렬로 실행되어 응답 시간을 최소화합니다
            - 메인 화면 레이아웃에 최적화된 데이터 구조를 제공합니다
            
            **📊 포함되는 차트**
            - **인기 영화**: 현재 가장 화제인 영화 상위 10개
            - **평점 높은 영화**: 사용자들이 높게 평가한 영화 상위 10개  
            - **인기 TV 시리즈**: 현재 가장 인기 있는 TV 시리즈 상위 10개
            - **평점 높은 TV 시리즈**: 사용자들이 높게 평가한 TV 시리즈 상위 10개
            
            **⚡ 성능 특징**
            - 단일 API 호출로 4개 차트의 데이터를 모두 제공
            - 캐싱 최적화로 빠른 응답 시간 보장
            - 메인 화면 초기 로딩 시간 단축에 기여
            
            **💡 사용 권장사항**
            - 메인 화면의 "추천 작품" 섹션에 사용
            - 사용자 대시보드의 "트렌드" 섹션에 활용
            - 앱 초기 화면의 콘텐츠 미리보기에 적합
            
            **🔄 업데이트**
            - 실시간 TMDB 데이터를 기반으로 지속적으로 업데이트
            - 캐시 TTL에 따라 정기적으로 새로운 데이터 반영
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "차트 요약 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChartSummaryResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 일부 차트 조회 실패"
            )
    })
    @GetMapping("/charts/summary")
    public Mono<ApiResponse<ChartSummaryResponse>> getChartSummary() {
        int summarySize = 10; // 요약 정보는 여전히 10개만 가져옴

        // getChart 호출 시 size 파라미터가 없으므로 서비스단에서 기본값(20)을 사용하게 됨.
        // 만약 요약 정보만 10개를 가져오고 싶다면, 서비스 메서드를 분리하거나 파라미터를 Optional로 받아야 함.
        // 여기서는 기존 로직을 유지하기 위해, 20개를 받아와서 10개만 사용하도록 Controller에서 처리

        Mono<List<MediaChartDto>> popularMovies = mediaQueryService.getChart(ChartType.POPULAR_MOVIES, 1)
                .map(p -> p.getContent().stream().limit(summarySize).toList());
        Mono<List<MediaChartDto>> topRatedMovies = mediaQueryService.getChart(ChartType.TOP_RATED_MOVIES, 1)
                .map(p -> p.getContent().stream().limit(summarySize).toList());
        Mono<List<MediaChartDto>> popularTv = mediaQueryService.getChart(ChartType.POPULAR_TV, 1)
                .map(p -> p.getContent().stream().limit(summarySize).toList());
        Mono<List<MediaChartDto>> topRatedTv = mediaQueryService.getChart(ChartType.TOP_RATED_TV, 1)
                .map(p -> p.getContent().stream().limit(summarySize).toList());

        return Mono.zip(popularMovies, topRatedMovies, popularTv, topRatedTv)
                .map(tuple -> new ChartSummaryResponse(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4()
                ))
                .map(ApiResponse::ok);
    }
}