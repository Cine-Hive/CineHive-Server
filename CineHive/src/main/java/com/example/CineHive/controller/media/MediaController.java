package com.example.CineHive.controller.media;

import com.example.CineHive.dto.media.MediaDetailsDto;
import com.example.CineHive.dto.media.MediaItemDto;
import com.example.CineHive.dto.media.MediaPageDto;
import com.example.CineHive.entity.media.Media;
import com.example.CineHive.exception.GenreNotFoundException;
import com.example.CineHive.service.media.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 미디어 정보 제공 API 컨트롤러
 * 영화, TV 프로그램, 애니메이션 관련 정보를 조회합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media Controller", description = "영화, TV 프로그램, 애니메이션 정보 제공 API")
public class MediaController {

    private final MediaService mediaService;

    /**
     * 미디어 상세 정보 조회
     *
     * @param mediaType 미디어 타입 (MOVIE, TV, ANIMATION)
     * @param id 미디어 ID
     * @return 미디어 상세 정보
     */
    @GetMapping("/{mediaType}s/{id}")
    @Operation(
            summary = "미디어 상세 정보 조회",
            description = "미디어 ID와 타입으로 상세 정보를 조회합니다. 영화, TV 프로그램, 애니메이션의 상세 정보를 제공합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaDetailsDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "영화 상세 정보 예시",
                                            value = "{\"id\": 505642, \"title\": \"블랙 팬서: 와칸다 포에버\", \"overview\": \"국왕이자 블랙 팬서인 티찰라의 죽음 이후 수많은 강대국으로부터 위협을 받게 된 와칸다...\", \"type\": \"MOVIE\", \"releaseDate\": \"2022-11-09\", \"voteAverage\": 7.3, \"posterPath\": \"/image.jpg\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "미디어를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"message\": \"해당 ID의 미디어를 찾을 수 없습니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "미디어 내용이 비어있음"
            )
    })
    public ResponseEntity<MediaDetailsDto> getMediaDetails(
            @Parameter(
                    description = "미디어 타입 (movie, tv, animation)",
                    schema = @Schema(type= "string"),
                    examples = {
                            @ExampleObject(name = "영화", value = "movie"),
                            @ExampleObject(name = "TV 프로그램", value = "tv"),
                            @ExampleObject(name = "애니메이션", value = "animation")
                    }
            )
            @PathVariable String mediaType,

            @Parameter(
                    description = "미디어 ID (TMDB ID)",
                    example = "505642"
            )
            @PathVariable Long id) {
        
        log.info("미디어 상세 정보 조회 요청: mediaType={}, id={}", mediaType, id);

        // 미디어 타입 문자열을 enum으로 변환
        Media.MediaType type = convertMediaType(mediaType);
        if (type == null) {
            log.warn("잘못된 미디어 타입: {}", mediaType);
            return ResponseEntity.badRequest().build();
        }

        MediaDetailsDto dto = mediaService.getMediaDetails(id, type);

        if (dto == null) {
            log.info("미디어를 찾을 수 없음: mediaType={}, id={}", mediaType, id);
            return ResponseEntity.notFound().build();
        }

        log.info("미디어 상세 정보 조회 성공: mediaType={}, id={}, title={}", mediaType, id, dto.getMediaInfo().getTitle());
        return ResponseEntity.ok(dto);
    }

    /**
     * 미디어 목록 조회
     *
     * @param mediaType 미디어 타입 (movie, tv, animation)
     * @param category 카테고리 (popular, top_rated, now_playing, upcoming)
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 미디어 목록
     */
    @GetMapping("/{mediaType}s")
    @Operation(
            summary = "미디어 목록 조회",
            description = "미디어 타입과 카테고리별로 목록을 조회합니다. 페이지 번호(1부터 시작)와 크기를 지정할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaPageDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "페이징된 인기 영화 목록 예시",
                                            value = "{\n" +
                                                    "  \"content\": [\n" +
                                                    "    {\"id\": 505642, \"title\": \"블랙 팬서: 와칸다 포에버\", \"type\": \"MOVIE\", \"posterPath\": \"/image.jpg\", \"voteAverage\": 7.3},\n" +
                                                    "    {\"id\": 76600, \"title\": \"아바타: 물의 길\", \"type\": \"MOVIE\", \"posterPath\": \"/image2.jpg\", \"voteAverage\": 8.0}\n" +
                                                    "  ],\n" +
                                                    "  \"pageable\": {\n" +
                                                    "    \"pageNumber\": 0,\n" +
                                                    "    \"pageSize\": 10\n" +
                                                    "  },\n" +
                                                    "  \"totalElements\": 100,\n" +
                                                    "  \"totalPages\": 10,\n" +
                                                    "  \"last\": false,\n" +
                                                    "  \"size\": 10,\n" +
                                                    "  \"number\": 0,\n" +
                                                    "  \"numberOfElements\": 10,\n" +
                                                    "  \"first\": true,\n" +
                                                    "  \"empty\": false\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"code\": \"INVALID_INPUT_VALUE\", \"message\": \"지원하지 않는 카테고리입니다.\", \"details\": null}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "검색 결과가 비어있음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaPageDto.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"page\": 1, \"results\": [], \"totalPages\": 0, \"totalResults\": 0}"
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<MediaPageDto> getMediaList(
            @Parameter(
                    description = "미디어 타입 (movie, tv, animation)",
                    schema = @Schema(type= "string"),
                    examples = {
                            @ExampleObject(name = "영화", value = "movie"),
                            @ExampleObject(name = "TV 프로그램", value = "tv"),
                            @ExampleObject(name = "애니메이션", value = "animation")
                    }
            )
            @PathVariable String mediaType,

            @Parameter(
                    description = "카테고리 (popular, top_rated, now_playing, upcoming)",
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "인기 있는", value = "popular"),
                            @ExampleObject(name = "최고 평점", value = "top_rated"),
                            @ExampleObject(name = "현재 상영/방영 중", value = "now_playing"),
                            @ExampleObject(name = "곧 개봉/방영 예정", value = "upcoming")
                    }
            )
            @RequestParam(defaultValue = "popular") String category,
            
            @Parameter(
                    description = "페이지 번호 (1부터 시작)",
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "50")
            )
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(
                    description = "페이지 크기", 
                    example = "20",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100")
            )
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("미디어 목록 조회 요청: mediaType={}, category={}, page={}, size={}", mediaType, category, page, size);
        
        // 미디어 타입 문자열을 enum으로 변환
        Media.MediaType type = convertMediaType(mediaType);
        if (type == null) {
            log.warn("잘못된 미디어 타입: {}", mediaType);
            return ResponseEntity.badRequest().build();
        }
        
        // 카테고리 문자열을 enum으로 변환
        Media.MediaCategory categoryEnum = convertCategory(category);
        if (categoryEnum == null) {
            log.warn("잘못된 카테고리: {}", category);
            return ResponseEntity.badRequest().build();
        }
        
        // 페이지 번호를 0-인덱스로 변환 (1 → 0, 2 → 1, ...)
        int pageIndex = Math.max(0, page - 1);
        
        // 페이지 요청 생성
        Pageable pageable = PageRequest.of(pageIndex, size);
        
        // 서비스 호출
        Page<MediaItemDto> mediaPage = mediaService.getMediaListPaged(type, categoryEnum, pageable);
        
        // Page<MediaItemDto>를 MediaPageDto로 변환
        MediaPageDto pageDto = MediaPageDto.builder()
                .page(pageable.getPageNumber() + 1) // 1부터 시작
                .results(mediaPage.getContent())
                .totalPages(mediaPage.getTotalPages())
                .totalResults((int) mediaPage.getTotalElements())
                .build();
        
        if (mediaPage.isEmpty()) {
            log.info("미디어 목록 조회 결과 없음: mediaType={}, category={}, page={}", mediaType, category, page);
        } else {
            log.info("미디어 목록 조회 성공: mediaType={}, category={}, page={}, 결과 수={}", 
                mediaType, category, page, mediaPage.getNumberOfElements());
        }
                
        return ResponseEntity.ok(pageDto);
    }

    /**
     * 미디어 검색
     *
     * @param query 검색 키워드 (2글자 이상)
     * @return 검색된 미디어 목록 (인기도 순으로 정렬)
     */
    @GetMapping("/search")
    @Operation(
            summary = "미디어 검색",
            description = "키워드로 미디어 제목을 검색합니다. 영화, TV 프로그램, 애니메이션을 통합 검색하고 인기도 순으로 정렬합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MediaItemDto.class)),
                            examples = {
                                    @ExampleObject(
                                            name = "검색 결과 예시",
                                            value = "[{\"id\": 76600, \"title\": \"아바타: 물의 길\", \"type\": \"MOVIE\", \"posterPath\": \"/image.jpg\", \"voteAverage\": 8.0, \"popularity\": 1842.5}, {\"id\": 19995, \"title\": \"아바타\", \"type\": \"MOVIE\", \"posterPath\": \"/image2.jpg\", \"voteAverage\": 7.5, \"popularity\": 756.3}]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"code\": \"INVALID_INPUT_VALUE\", \"message\": \"검색어는 2글자 이상이어야 합니다.\", \"details\": null}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "검색 결과가 비어있음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            value = "[]"
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<List<MediaItemDto>> searchMedia(
            @Parameter(
                    description = "검색 키워드 (최소 2글자 이상)",
                    example = "아바타",
                    required = true
            )
            @RequestParam String query) {

        log.info("미디어 검색 요청: query={}", query);
        
        // 검색어 길이 검증
        if (query == null || query.trim().length() < 2) {
            log.warn("검색어 길이가 너무 짧음: {}", query);
            return ResponseEntity.badRequest().build();
        }

        List<MediaItemDto> searchResults = mediaService.searchMedia(query);
        
        if (searchResults.isEmpty()) {
            log.info("검색 결과 없음: query={}", query);
        } else {
            log.info("검색 성공: query={}, 결과 수={}", query, searchResults.size());
        }
        
        return ResponseEntity.ok(searchResults);
    }
    
    /**
     * 장르별 미디어 목록 조회
     *
     * @param mediaType 미디어 타입 (movie, tv, animation)
     * @param genreId 장르 ID
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 페이징된 장르별 미디어 목록
     */
    @GetMapping("/{mediaType}s/genre/{genreId}")
    @Operation(
            summary = "장르별 미디어 목록 조회",
            description = "장르 ID와 미디어 타입으로 목록을 조회합니다. 페이지 번호(1부터 시작)와 크기를 지정할 수 있습니다. " +
                    "특별히 애니메이션 장르(16)의 경우 ANIMATION 타입으로 자동 처리됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaPageDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "페이징된 액션 영화 목록 예시",
                                            value = "{\n" +
                                                    "  \"content\": [\n" +
                                                    "    {\"id\": 505642, \"title\": \"블랙 팬서: 와칸다 포에버\", \"type\": \"MOVIE\", \"posterPath\": \"/image.jpg\", \"voteAverage\": 7.3},\n" +
                                                    "    {\"id\": 76600, \"title\": \"아바타: 물의 길\", \"type\": \"MOVIE\", \"posterPath\": \"/image2.jpg\", \"voteAverage\": 8.0}\n" +
                                                    "  ],\n" +
                                                    "  \"pageable\": {\n" +
                                                    "    \"pageNumber\": 0,\n" +
                                                    "    \"pageSize\": 20\n" +
                                                    "  },\n" +
                                                    "  \"totalElements\": 100,\n" +
                                                    "  \"totalPages\": 5,\n" +
                                                    "  \"last\": false,\n" +
                                                    "  \"size\": 20,\n" +
                                                    "  \"number\": 0,\n" +
                                                    "  \"numberOfElements\": 20,\n" +
                                                    "  \"first\": true,\n" +
                                                    "  \"empty\": false\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(
                                            name = "잘못된 미디어 타입",
                                            value = "{\"code\": \"INVALID_INPUT_VALUE\", \"message\": \"지원하지 않는 미디어 타입입니다.\", \"details\": null}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "장르를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"code\":\"GENRE_NOT_FOUND\",\"message\":\"해당 ID의 장르를 찾을 수 없습니다.\",\"details\":null}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "장르에 해당하는 미디어가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MediaPageDto.class),
                            examples = {
                                    @ExampleObject(
                                            value = "{\"page\": 1, \"results\": [], \"totalPages\": 0, \"totalResults\": 0}"
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<Object> getMediaListByGenre(
            @Parameter(
                    description = "미디어 타입 (movie, tv, animation)",
                    schema = @Schema(type= "string"),
                    examples = {
                            @ExampleObject(name = "영화", value = "movie"),
                            @ExampleObject(name = "TV 프로그램", value = "tv"),
                            @ExampleObject(name = "애니메이션", value = "animation")
                    }
            )
            @PathVariable String mediaType,

            @Parameter(
                    description = "장르 ID",
                    example = "28",
                    schema = @Schema(description = "장르 ID (TMDB 기준 - 영화: 28(액션), 12(모험) 등, TV: 10759(액션&어드벤처) 등)")
            )
            @PathVariable Integer genreId,
            
            @Parameter(
                    description = "페이지 번호 (1부터 시작)",
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "50")
            )
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(
                    description = "페이지 크기", 
                    example = "20",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100")
            )
            @RequestParam(defaultValue = "20") int size) {

        log.info("장르별 미디어 목록 조회 요청: mediaType={}, genreId={}, page={}, size={}", mediaType, genreId, page, size);
        
        try {
            // 미디어 타입 문자열을 enum으로 변환
            Media.MediaType type = convertMediaType(mediaType);
            if (type == null) {
                log.warn("잘못된 미디어 타입: {}", mediaType);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", "INVALID_INPUT_VALUE");
                errorResponse.put("message", "지원하지 않는 미디어 타입입니다.");
                errorResponse.put("details", null);
                return ResponseEntity.badRequest().body(errorResponse);
            }
    
            // 페이지 번호 조정 (1부터 시작하는 UI 페이지 → 0부터 시작하는 Spring 페이지)
            Pageable pageable = PageRequest.of(page - 1, size);
            
            // 장르별 미디어 목록 조회 서비스 호출
            Page<MediaItemDto> result = mediaService.getMediaListByGenrePaged(genreId, type, pageable);
            
            // Page<MediaItemDto>를 MediaPageDto로 변환
            MediaPageDto pageDto = MediaPageDto.builder()
                    .page(pageable.getPageNumber() + 1) // 1부터 시작
                    .results(result.getContent())
                    .totalPages(result.getTotalPages())
                    .totalResults((int) result.getTotalElements())
                    .build();
            
            if (result.isEmpty()) {
                log.info("장르별 미디어 목록 조회 결과 없음: mediaType={}, genreId={}, page={}", mediaType, genreId, page);
            } else {
                log.info("장르별 미디어 목록 조회 성공: mediaType={}, genreId={}, page={}, 결과 수={}", 
                    mediaType, genreId, page, result.getNumberOfElements());
            }
                    
            return ResponseEntity.ok(pageDto);
        } catch (GenreNotFoundException e) {
            log.warn("장르를 찾을 수 없음: genreId={}, message={}", genreId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "GENRE_NOT_FOUND");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            log.error("장르별 미디어 목록 조회 중 오류 발생: mediaType={}, genreId={}", mediaType, genreId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 미디어 타입 문자열을 enum으로 변환
     */
    private Media.MediaType convertMediaType(String mediaType) {
        if (mediaType == null) {
            return null;
        }
        
        return switch (mediaType.toLowerCase()) {
            case "movie" -> Media.MediaType.MOVIE;
            case "tv" -> Media.MediaType.TV;
            case "animation" -> Media.MediaType.ANIMATION;
            default -> null;
        };
    }
    
    /**
     * 카테고리 문자열을 enum으로 변환
     */
    private Media.MediaCategory convertCategory(String category) {
        if (category == null) {
            return null;
        }
        
        return switch (category.toLowerCase()) {
            case "popular" -> Media.MediaCategory.POPULAR;
            case "top_rated" -> Media.MediaCategory.TOP_RATED;
            case "now_playing" -> Media.MediaCategory.NOW_PLAYING;
            case "upcoming" -> Media.MediaCategory.UPCOMING;
            case "on_the_air" -> Media.MediaCategory.ON_THE_AIR;
            case "airing_today" -> Media.MediaCategory.AIRING_TODAY;
            default -> null;
        };
    }
}