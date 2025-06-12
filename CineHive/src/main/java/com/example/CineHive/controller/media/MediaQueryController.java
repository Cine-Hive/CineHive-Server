package com.example.CineHive.controller.media;

import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.dto.response.MediaChartDto;
import com.example.CineHive.dto.response.MediaDetailDto;
import com.example.CineHive.dto.response.MediaSummaryDto;
import com.example.CineHive.dto.response.PagedResponse;
import com.example.CineHive.service.media.MediaQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Tag(name = "Media Query Controller", description = "영화 및 TV시리즈 미디어 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaQueryController {

    private final MediaQueryService mediaQueryService;

    /**
     * 미디어 상세 정보 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaDetailDto>> getMediaDetail(
            @PathVariable Long id,
            @RequestParam @NotBlank(message = "미디어 타입은 필수입니다.") String mediaType) {
        try {
            MediaDetailDto mediaDetail = mediaQueryService.getMediaDetail(id, mediaType);
            return ResponseEntity.ok(ApiResponse.ok(mediaDetail));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for media detail - id: {}, mediaType: {}, error: {}",
                    id, mediaType, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving media detail for id: {}, mediaType: {}", id, mediaType, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("미디어 상세 정보를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 인기 영화 차트 조회
     */
    @GetMapping("/movies/popular")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getPopularMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> popularMovies = mediaQueryService.getPopularMovieChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(popularMovies));
        } catch (Exception e) {
            log.error("Error retrieving popular movies", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("인기 영화 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 평점 높은 영화 차트 조회
     */
    @GetMapping("/movies/top-rated")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getTopRatedMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> topRatedMovies = mediaQueryService.getTopRatedMovieChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(topRatedMovies));
        } catch (Exception e) {
            log.error("Error retrieving top rated movies", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("평점 높은 영화 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 개봉 예정 영화 차트 조회
     */
    @GetMapping("/movies/upcoming")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getUpcomingMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> upcomingMovies = mediaQueryService.getUpcomingMovieChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(upcomingMovies));
        } catch (Exception e) {
            log.error("Error retrieving upcoming movies", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("개봉 예정 영화 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 현재 상영중 영화 차트 조회
     */
    @GetMapping("/movies/now-playing")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getNowPlayingMovies(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> nowPlayingMovies = mediaQueryService.getNowPlayingMovieChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(nowPlayingMovies));
        } catch (Exception e) {
            log.error("Error retrieving now playing movies", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("현재 상영중 영화 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 인기 TV 시리즈 차트 조회
     */
    @GetMapping("/tv/popular")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getPopularTvSeries(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> popularTvSeries = mediaQueryService.getPopularTvSeriesChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(popularTvSeries));
        } catch (Exception e) {
            log.error("Error retrieving popular TV series", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("인기 TV 시리즈 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 평점 높은 TV 시리즈 차트 조회
     */
    @GetMapping("/tv/top-rated")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getTopRatedTvSeries(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> topRatedTvSeries = mediaQueryService.getTopRatedTvSeriesChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(topRatedTvSeries));
        } catch (Exception e) {
            log.error("Error retrieving top rated TV series", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("평점 높은 TV 시리즈 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 방영중 TV 시리즈 차트 조회
     */
    @GetMapping("/tv/on-the-air")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getOnTheAirTvSeries(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> onTheAirTvSeries = mediaQueryService.getOnTheAirTvSeriesChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(onTheAirTvSeries));
        } catch (Exception e) {
            log.error("Error retrieving on the air TV series", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("방영중 TV 시리즈 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 오늘 방영 TV 시리즈 차트 조회
     */
    @GetMapping("/tv/airing-today")
    public ResponseEntity<ApiResponse<PagedResponse<MediaChartDto>>> getAiringTodayTvSeries(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaChartDto> airingTodayTvSeries = mediaQueryService.getAiringTodayTvSeriesChart(page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(airingTodayTvSeries));
        } catch (Exception e) {
            log.error("Error retrieving airing today TV series", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("오늘 방영 TV 시리즈 차트를 가져오는 중 오류가 발생했습니다."));
        }
    }

    /**
     * 미디어 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<MediaSummaryDto>>> searchMedia(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        try {
            PagedResponse<MediaSummaryDto> searchResults = mediaQueryService.searchMedia(query, page - 1, size);
            return ResponseEntity.ok(ApiResponse.ok(searchResults));
        } catch (Exception e) {
            log.error("Error searching media with query: {}", query, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("미디어 검색 중 오류가 발생했습니다."));
        }
    }
}