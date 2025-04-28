package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.CastDto;
import com.example.CineHive.dto.media.CrewDto;
import com.example.CineHive.dto.media.GenreDto;
import com.example.CineHive.dto.media.MediaCreditsDto;
import com.example.CineHive.dto.media.MediaDetailsDto;
import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.dto.media.MediaItemDto;
import com.example.CineHive.dto.media.VideoDto;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.service.media.MediaService;
import com.example.CineHive.service.media.TmdbMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

    @RestController
    @RequestMapping("/api/v1")
    public class MediaController {

        @Autowired
        private MediaService mediaService;

    // 영화 API 엔드포인트

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(
        summary = "영화 정보 조회",
        description = "영화의 기본 정보를 제공하거나, 쿼리 파라미터를 통해 상세 정보(출연/제작진, 비디오, 유사 영화)를 함께 제공합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "영화 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "영화를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/movies/{id}")
    public ResponseEntity<?> getMovie(
            @Parameter(description = "영화 ID", example = "550") @PathVariable Long id,
            @Parameter(description = "상세 정보 포함 여부 (true: 출연/제작진, 비디오, 유사 영화 포함)", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean details) {

        if (details) {
            MediaDetailsDto detailsDto = mediaService.getMediaDetails(Media.MediaType.MOVIE, id);
            if (detailsDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(detailsDto);
        } else {
            MediaItemDto mediaItemDto = mediaService.getMediaById(Media.MediaType.MOVIE, id);
            if (mediaItemDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mediaItemDto);
        }
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(
        summary = "인기 영화 목록 조회",
        description = "현재 인기 있는 영화 목록을 페이지 단위로 제공합니다. 각 영화에는 기본 정보가 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인기 영화 목록 조회 성공",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MediaDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/movies/popular")
    public ResponseEntity<MediaDto> getPopularMovies(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "popularity.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.MOVIE, Media.MediaCategory.POPULAR, page, sort));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "평점 높은 영화 목록 조회", description = "평점이 높은 영화 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평점 높은 영화 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/movies/top-rated")
    public ResponseEntity<MediaDto> getTopRatedMovies(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "vote_average.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.MOVIE, Media.MediaCategory.TOP_RATED, page, sort));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "현재 상영 중인 영화 목록 조회", description = "현재 상영 중인 영화 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "현재 상영 중인 영화 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/movies/now-playing")
    public ResponseEntity<MediaDto> getNowPlayingMovies(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "release_date.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.MOVIE, Media.MediaCategory.NOW_PLAYING, page, sort));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "개봉 예정 영화 목록 조회", description = "개봉 예정인 영화 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "개봉 예정 영화 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/movies/upcoming")
    public ResponseEntity<MediaDto> getUpcomingMovies(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "release_date.asc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.MOVIE, Media.MediaCategory.UPCOMING, page, sort));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "유사 영화 목록 조회", description = "특정 영화와 유사한 영화 목록을 페이지 단위로 제공")
    @GetMapping("/movies/{id}/similar")
    public ResponseEntity<MediaDto> getSimilarMovies(
            @Parameter(description = "영화 ID") @PathVariable Long id,
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.getSimilarMedia(Media.MediaType.MOVIE, id, page));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "영화 출연/제작진 정보 조회", description = "특정 영화의 출연진과 제작진 정보를 제공")
    @GetMapping("/movies/{id}/credits")
    public ResponseEntity<MediaCreditsDto> getMovieCredits(
            @Parameter(description = "영화 ID") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaWithCredits(Media.MediaType.MOVIE, id));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "영화 비디오 정보 조회", description = "특정 영화의 트레일러 등 비디오 정보를 제공")
    @GetMapping("/movies/{id}/videos")
    public ResponseEntity<List<VideoDto>> getMovieVideos(
            @Parameter(description = "영화 ID") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaVideos(Media.MediaType.MOVIE, id));
    }

    @Tag(name = "Movie Controller", description = "영화 정보를 제공하는 API")
    @Operation(summary = "영화 검색", description = "제목 기반으로 영화를 검색")
    @GetMapping("/movies/search")
    public ResponseEntity<MediaDto> searchMovies(
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.searchMedia(Media.MediaType.MOVIE, query, page));
    }

    // TV 시리즈 API 엔드포인트

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(
        summary = "TV 시리즈 정보 조회",
        description = "TV 시리즈의 기본 정보를 제공하거나, 쿼리 파라미터를 통해 상세 정보(출연/제작진, 비디오, 유사 TV 시리즈)를 함께 제공합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "TV 시리즈 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "TV 시리즈를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/tv/{id}")
    public ResponseEntity<?> getTv(
            @Parameter(description = "TV 시리즈 ID", example = "1396") @PathVariable Long id,
            @Parameter(description = "상세 정보 포함 여부 (true: 출연/제작진, 비디오, 유사 TV 시리즈 포함)", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean details) {

        if (details) {
            MediaDetailsDto detailsDto = mediaService.getMediaDetails(Media.MediaType.TV, id);
            if (detailsDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(detailsDto);
        } else {
            MediaItemDto mediaItemDto = mediaService.getMediaById(Media.MediaType.TV, id);
            if (mediaItemDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mediaItemDto);
        }
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "인기 TV 시리즈 목록 조회", description = "인기 있는 TV 시리즈 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인기 TV 시리즈 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/tv/popular")
    public ResponseEntity<MediaDto> getPopularTvSeries(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "popularity.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "first_air_date.desc", "first_air_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.TV, Media.MediaCategory.POPULAR, page, sort));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "평점 높은 TV 시리즈 목록 조회", description = "평점이 높은 TV 시리즈 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평점 높은 TV 시리즈 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/tv/top-rated")
    public ResponseEntity<MediaDto> getTopRatedTvSeries(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "vote_average.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "first_air_date.desc", "first_air_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.TV, Media.MediaCategory.TOP_RATED, page, sort));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "방영 중인 TV 시리즈 목록 조회", description = "현재 방영 중인 TV 시리즈 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "방영 중인 TV 시리즈 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/tv/on-the-air")
    public ResponseEntity<MediaDto> getOnTheAirTvSeries(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "first_air_date.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "first_air_date.desc", "first_air_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.TV, Media.MediaCategory.ON_THE_AIR, page, sort));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "오늘 방영하는 TV 시리즈 목록 조회", description = "오늘 방영하는 TV 시리즈 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "오늘 방영하는 TV 시리즈 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/tv/airing-today")
    public ResponseEntity<MediaDto> getAiringTodayTvSeries(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "popularity.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "first_air_date.desc", "first_air_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getMediaByCategory(Media.MediaType.TV, Media.MediaCategory.AIRING_TODAY, page, sort));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "유사 TV 시리즈 목록 조회", description = "특정 TV 시리즈와 유사한 시리즈 목록을 페이지 단위로 제공")
    @GetMapping("/tv/{id}/similar")
    public ResponseEntity<MediaDto> getSimilarTvSeries(
            @Parameter(description = "TV 시리즈 ID") @PathVariable Long id,
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.getSimilarMedia(Media.MediaType.TV, id, page));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "TV 시리즈 출연/제작진 정보 조회", description = "특정 TV 시리즈의 출연진과 제작진 정보를 제공")
    @GetMapping("/tv/{id}/credits")
    public ResponseEntity<MediaCreditsDto> getTvCredits(
            @Parameter(description = "TV 시리즈 ID") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaWithCredits(Media.MediaType.TV, id));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "TV 시리즈 비디오 정보 조회", description = "특정 TV 시리즈의 트레일러 등 비디오 정보를 제공")
    @GetMapping("/tv/{id}/videos")
    public ResponseEntity<List<VideoDto>> getTvVideos(
            @Parameter(description = "TV 시리즈 ID") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaVideos(Media.MediaType.TV, id));
    }

    @Tag(name = "TV Series Controller", description = "TV 시리즈 정보를 제공하는 API")
    @Operation(summary = "TV 시리즈 검색", description = "제목 기반으로 TV 시리즈를 검색")
    @GetMapping("/tv/search")
    public ResponseEntity<MediaDto> searchTvSeries(
            @Parameter(description = "검색어") @RequestParam String query,
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.searchMedia(Media.MediaType.TV, query, page));
    }

    // 애니메이션 API 엔드포인트

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "애니메이션 정보 조회",
        description = "애니메이션의 기본 정보를 제공하거나, 쿼리 파라미터를 통해 상세 정보(출연/제작진, 비디오, 유사 애니메이션)를 함께 제공합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "애니메이션 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "애니메이션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/{id}")
    public ResponseEntity<?> getAnimation(
            @Parameter(description = "애니메이션 ID", example = "129") @PathVariable Long id,
            @Parameter(description = "상세 정보 포함 여부 (true: 출연/제작진, 비디오, 유사 애니메이션 포함)", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean details) {

        if (details) {
            MediaDetailsDto detailsDto = mediaService.getMediaDetails(Media.MediaType.ANIMATION, id);
            if (detailsDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(detailsDto);
        } else {
            MediaItemDto mediaItemDto = mediaService.getMediaById(Media.MediaType.ANIMATION, id);
            if (mediaItemDto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mediaItemDto);
        }
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(summary = "인기 애니메이션 목록 조회", description = "인기 있는 애니메이션 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인기 애니메이션 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/popular")
    public ResponseEntity<MediaDto> getPopularAnimations(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "popularity.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getAnimationsByCategory(Media.MediaCategory.POPULAR, page, sort));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(summary = "평점 높은 애니메이션 목록 조회", description = "평점이 높은 애니메이션 목록을 페이지 단위로 제공")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "평점 높은 애니메이션 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/top-rated")
    public ResponseEntity<MediaDto> getTopRatedAnimations(
            @Parameter(description = "페이지 번호 (기본값 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "vote_average.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getAnimationsByCategory(Media.MediaCategory.TOP_RATED, page, sort));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "상영/방영 중인 애니메이션 목록 조회",
        description = "현재 상영/방영 중인 애니메이션 목록을 페이지 단위로 제공합니다. 극장 개봉작과 TV 방영작을 모두 포함합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상영/방영 중인 애니메이션 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/now-playing")
    public ResponseEntity<MediaDto> getNowPlayingAnimations(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "release_date.desc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getAnimationsByCategory(Media.MediaCategory.NOW_PLAYING, page, sort));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "개봉/방영 예정 애니메이션 목록 조회",
        description = "개봉/방영 예정인 애니메이션 목록을 페이지 단위로 제공합니다. 개봉일 순으로 정렬됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "개봉/방영 예정 애니메이션 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "결과 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/upcoming")
    public ResponseEntity<MediaDto> getUpcomingAnimations(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "정렬 방식", example = "release_date.asc",
                schema = @Schema(allowableValues = {"popularity.desc", "popularity.asc", "vote_average.desc", "vote_average.asc",
                    "release_date.desc", "release_date.asc", "vote_count.desc"}))
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(mediaService.getAnimationsByCategory(Media.MediaCategory.UPCOMING, page, sort));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(summary = "유사 애니메이션 목록 조회", description = "특정 애니메이션과 유사한 애니메이션 목록을 페이지 단위로 제공합니다. 유사도는 장르, 키워드, 스튜디오 등을 기준으로 계산됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "유사 애니메이션 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "기준 애니메이션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/{id}/similar")
    public ResponseEntity<MediaDto> getSimilarAnimations(
            @Parameter(description = "애니메이션 ID", example = "129") @PathVariable Long id,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.getSimilarMedia(Media.MediaType.ANIMATION, id, page));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "애니메이션 출연/제작진 정보 조회",
        description = "특정 애니메이션의 성우와 제작진 정보를 제공합니다. 성우, 감독, 작가, 애니메이터 등의 정보가 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "애니메이션 출연/제작진 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "애니메이션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/{id}/credits")
    public ResponseEntity<MediaCreditsDto> getAnimationCredits(
            @Parameter(description = "애니메이션 ID", example = "129") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaWithCredits(Media.MediaType.ANIMATION, id));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "애니메이션 비디오 정보 조회",
        description = "특정 애니메이션의 트레일러, 티저, 메이킹 등 관련 비디오 정보를 제공합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "애니메이션 비디오 정보 조회 성공"),
        @ApiResponse(responseCode = "404", description = "애니메이션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/{id}/videos")
    public ResponseEntity<List<VideoDto>> getAnimationVideos(
            @Parameter(description = "애니메이션 ID", example = "129") @PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaVideos(Media.MediaType.ANIMATION, id));
    }

    @Tag(name = "Animation Controller", description = "애니메이션 정보를 제공하는 API")
    @Operation(
        summary = "애니메이션 검색",
        description = "제목 기반으로 애니메이션을 검색합니다. 부분 일치하는 모든 애니메이션이 결과에 포함됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "애니메이션 검색 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/animations/search")
    public ResponseEntity<MediaDto> searchAnimations(
            @Parameter(description = "검색어", example = "원피스") @RequestParam String query,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(mediaService.searchAnimations(query, page));
    }

    // 관리자용 API 엔드포인트

    @Tag(name = "Admin Controller", description = "미디어 데이터 관리를 위한 관리자용 API")
    @Operation(summary = "미디어 데이터 동기화", description = "TMDB API에서 최신 데이터를 가져와 DB에 동기화 (관리자용)")
    @PostMapping("/admin/media/sync")
    public ResponseEntity<String> syncMediaData(
            @Parameter(description = "미디어 타입 (MOVIE, TV, ANIMATION)") @RequestParam Media.MediaType mediaType) {

        java.util.Set<Media.MediaCategory> availableCategories;

        switch (mediaType) {
            case MOVIE:
                availableCategories = java.util.Set.of(
                    Media.MediaCategory.POPULAR,
                    Media.MediaCategory.TOP_RATED,
                    Media.MediaCategory.NOW_PLAYING,
                    Media.MediaCategory.UPCOMING
                );
                break;
            case TV:
                availableCategories = java.util.Set.of(
                    Media.MediaCategory.POPULAR,
                    Media.MediaCategory.TOP_RATED,
                    Media.MediaCategory.ON_THE_AIR,
                    Media.MediaCategory.AIRING_TODAY
                );
                break;
            case ANIMATION:
                availableCategories = java.util.Set.of(
                    Media.MediaCategory.POPULAR,
                    Media.MediaCategory.TOP_RATED
                );
                break;
            default:
                return ResponseEntity.badRequest().body("지원하지 않는 미디어 타입입니다.");
        }

        for (Media.MediaCategory category : availableCategories) {
            mediaService.syncMediaData(mediaType, category);
        }

        return ResponseEntity.ok(mediaType + " 미디어 데이터 동기화 완료");
    }

    // 추천 관리자 API 엔드포인트

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "추천 정보 강제 갱신", description = "특정 미디어의 추천 정보를 TMDB API에서 다시 가져와 갱신 (관리자용)")
    @PostMapping("/admin/recommendations/refresh")
    public ResponseEntity<String> refreshRecommendations(
            @Parameter(description = "미디어 타입 (MOVIE, TV, ANIMATION)") @RequestParam Media.MediaType mediaType,
            @Parameter(description = "미디어 ID") @RequestParam Long mediaId) {
        mediaService.refreshRecommendations(mediaType, mediaId);
        return ResponseEntity.ok(mediaType + " ID: " + mediaId + "의 추천 정보 갱신 완료");
    }

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "추천 정보 삭제", description = "특정 미디어의 추천 정보를 삭제 (관리자용)")
    @DeleteMapping("/admin/recommendations")
    public ResponseEntity<String> deleteRecommendations(
            @Parameter(description = "미디어 타입 (MOVIE, TV, ANIMATION)") @RequestParam Media.MediaType mediaType,
            @Parameter(description = "미디어 ID") @RequestParam Long mediaId) {
        mediaService.deleteRecommendations(mediaType, mediaId);
        return ResponseEntity.ok(mediaType + " ID: " + mediaId + "의 추천 정보 삭제 완료");
    }

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "추천 정보 통계 조회", description = "추천 정보 시스템의 현재 상태 및 통계 정보 조회 (관리자용)")
    @GetMapping("/admin/recommendations/stats")
    public ResponseEntity<Map<String, Object>> getRecommendationStats() {
        return ResponseEntity.ok(mediaService.getRecommendationStats());
    }

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "접근 빈도 기준 변경", description = "추천 정보 삭제를 위한 최소 접근 빈도 기준 변경 (관리자용)")
    @PutMapping("/admin/recommendations/threshold")
    public ResponseEntity<String> updateAccessCountThreshold(
            @Parameter(description = "최소 접근 횟수 기준 (기본값: 3)") @RequestParam int threshold) {
        mediaService.updateAccessCountThreshold(threshold);
        return ResponseEntity.ok("접근 빈도 기준이 " + threshold + "회로 변경되었습니다.");
    }

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "만료 기간 변경", description = "추천 정보의 기본 만료 기간 변경 (관리자용)")
    @PutMapping("/admin/recommendations/expiry")
    public ResponseEntity<String> updateExpiryDays(
            @Parameter(description = "만료 기간 (일 단위, 기본값: 30)") @RequestParam int days) {
        mediaService.updateExpiryDays(days);
        return ResponseEntity.ok("추천 정보 만료 기간이 " + days + "일로 변경되었습니다.");
    }

    @Tag(name = "Admin Controller", description = "추천 기능 관리를 위한 관리자용 API")
    @Operation(summary = "추천 정보 수동 정리", description = "만료되거나 접근 빈도가 낮은 추천 정보를 즉시 정리 (관리자용)")
    @DeleteMapping("/admin/recommendations/cleanup")
    public ResponseEntity<String> cleanupRecommendations() {
        // 스케줄링된 작업을 즉시 실행
        if (mediaService instanceof TmdbMediaService) {
            ((TmdbMediaService) mediaService).cleanExpiredRecommendations();
            return ResponseEntity.ok("추천 정보 정리 완료");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("지원되지 않는 서비스 구현");
        }
    }
} 