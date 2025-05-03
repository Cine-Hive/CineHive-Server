package com.example.CineHive.controller.media;

import com.example.CineHive.entity.media.Media;
import com.example.CineHive.service.media.MediaSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 미디어 데이터 동기화 컨트롤러
 * TMDB API로부터 미디어 데이터를 가져와 데이터베이스에 저장합니다.
 */
@RestController
@RequestMapping("/api/v1/admin/sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Controller", description = "영화, TV 시리즈, 애니메이션 데이터 관리 API")
public class MediaSyncController {
    
    private final MediaSyncService mediaSyncService;
    
    /**
     * API 응답용 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class ApiResponseDto {
        private boolean success;
        private String message;
    }
    
    /**
     * 미디어 데이터 동기화 API
     * TMDB API에서 최신 미디어 데이터를 가져와 데이터베이스에 저장합니다.
     *
     * @param mediaType 동기화할 미디어 타입 (MOVIE, TV, ANIMATION)
     * @param category 동기화할 카테고리 (POPULAR, TOP_RATED 등)
     * @param maxPages 동기화할 최대 페이지 수
     * @return 동기화 결과 메시지
     */
    @PostMapping("/{mediaType}s/{category}")
    @Operation(
        summary = "미디어 데이터 동기화", 
        description = "TMDB API에서 미디어 데이터를 가져와 데이터베이스에 저장합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "동기화 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponseDto> syncMediaData(
            @Parameter(
                description = "동기화할 미디어 타입", 
                schema = @Schema(implementation = Media.MediaType.class),
                examples = {
                    @ExampleObject(name = "영화", value = "MOVIE", summary = "영화 데이터 동기화"),
                    @ExampleObject(name = "TV 시리즈", value = "TV", summary = "TV 시리즈 데이터 동기화"),
                    @ExampleObject(name = "애니메이션", value = "ANIMATION", summary = "애니메이션 데이터 동기화")
                }
            )
            @PathVariable Media.MediaType mediaType,
            
            @Parameter(
                description = "동기화할 미디어 카테고리", 
                schema = @Schema(implementation = Media.MediaCategory.class),
                examples = {
                    @ExampleObject(name = "인기작", value = "POPULAR", summary = "인기 있는 미디어 동기화"),
                    @ExampleObject(name = "평점순", value = "TOP_RATED", summary = "높은 평점의 미디어 동기화"),
                    @ExampleObject(name = "현재 상영작", value = "NOW_PLAYING", summary = "현재 상영 중인 미디어 동기화"),
                    @ExampleObject(name = "개봉 예정작", value = "UPCOMING", summary = "개봉 예정인 미디어 동기화")
                }
            )
            @PathVariable Media.MediaCategory category,
            
            @Parameter(
                description = "동기화할 최대 페이지 수 (기본값: 5, 최대: 20)",
                example = "5"
            )
            @RequestParam(required = false, defaultValue = "5") Integer maxPages) {
        
        try {
            // 미디어 타입과 카테고리 조합 유효성 검사
            if (!isValidCombination(mediaType, category)) {
                String errorMessage = String.format("미디어 타입 '%s'에 대해 유효하지 않은 카테고리 '%s'입니다.", 
                                                   mediaType, category);
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto(false, errorMessage));
            }
            
            // 페이지 수 제한 (1~20)
            int pages = Math.min(Math.max(maxPages, 1), 20);
            
            log.info("미디어 동기화 시작: 타입={}, 카테고리={}, 페이지={}", mediaType, category, pages);
            mediaSyncService.syncMediaData(mediaType, category, pages);
            
            String successMessage = String.format("미디어 데이터 동기화가 완료되었습니다. 타입: %s, 카테고리: %s, 페이지 수: %d", 
                                                  mediaType, category, pages);
            return ResponseEntity.ok(new ApiResponseDto(true, successMessage));
        } catch (Exception e) {
            log.error("미디어 데이터 동기화 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                                 .body(new ApiResponseDto(false, "미디어 데이터 동기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 특정 미디어 데이터 동기화 API
     * TMDB API에서 특정 ID의 미디어 데이터를 가져와 데이터베이스에 저장합니다.
     *
     * @param mediaId 동기화할 TMDB 미디어 ID
     * @param mediaType 동기화할 미디어 타입 (MOVIE, TV, ANIMATION)
     * @return 동기화 결과 메시지
     */
    @PostMapping("/{mediaType}/{mediaId}")
    @Operation(
        summary = "특정 미디어 데이터 동기화", 
        description = "TMDB API에서 특정 미디어 데이터를 가져와 데이터베이스에 저장합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "동기화 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "404", description = "미디어를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ApiResponseDto> syncSingleMedia(
            @Parameter(
                description = "동기화할 미디어 타입", 
                schema = @Schema(implementation = Media.MediaType.class),
                examples = {
                    @ExampleObject(name = "영화", value = "MOVIE", summary = "영화 데이터 동기화"),
                    @ExampleObject(name = "TV 시리즈", value = "TV", summary = "TV 시리즈 데이터 동기화"),
                    @ExampleObject(name = "애니메이션", value = "ANIMATION", summary = "애니메이션 데이터 동기화")
                }
            )
            @PathVariable Media.MediaType mediaType,
            
            @Parameter(
                description = "동기화할 TMDB 미디어 ID",
                example = "550"
            )
            @PathVariable Long mediaId) {
        
        try {
            log.info("단일 미디어 동기화 시작: ID={}, 타입={}", mediaId, mediaType);
            
            boolean success = mediaSyncService.syncSingleMedia(mediaId, mediaType);
            
            if (success) {
                String successMessage = String.format("미디어 데이터 동기화가 완료되었습니다. ID: %d, 타입: %s", 
                                                    mediaId, mediaType);
                return ResponseEntity.ok(new ApiResponseDto(true, successMessage));
            } else {
                String errorMessage = String.format("TMDB에서 해당 미디어를 찾을 수 없습니다. ID: %d, 타입: %s", 
                                                   mediaId, mediaType);
                return ResponseEntity.status(404)
                    .body(new ApiResponseDto(false, errorMessage));
            }
        } catch (Exception e) {
            log.error("단일 미디어 데이터 동기화 중 오류 발생: ID={}, 타입={}", mediaId, mediaType, e);
            return ResponseEntity.internalServerError()
                                .body(new ApiResponseDto(false, "미디어 데이터 동기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    /**
     * 미디어 타입과 카테고리 조합이 유효한지 검사합니다.
     */
    private boolean isValidCombination(Media.MediaType mediaType, Media.MediaCategory category) {
        return switch (mediaType) {
            case MOVIE -> category == Media.MediaCategory.POPULAR || 
                         category == Media.MediaCategory.TOP_RATED || 
                         category == Media.MediaCategory.NOW_PLAYING || 
                         category == Media.MediaCategory.UPCOMING;
                    
            case TV -> category == Media.MediaCategory.POPULAR || 
                      category == Media.MediaCategory.TOP_RATED || 
                      category == Media.MediaCategory.ON_THE_AIR || 
                      category == Media.MediaCategory.AIRING_TODAY;
                    
            case ANIMATION -> category == Media.MediaCategory.POPULAR || 
                             category == Media.MediaCategory.TOP_RATED || 
                             category == Media.MediaCategory.NOW_PLAYING || 
                             category == Media.MediaCategory.UPCOMING;
        };
    }
}