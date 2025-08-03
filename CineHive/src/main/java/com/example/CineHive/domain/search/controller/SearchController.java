package com.example.CineHive.domain.search.controller.entity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서비스의 모든 검색 기능을 제공하는 API 컨트롤러입니다.
 */
@Tag(name = "Search Controller", description = "통합 검색 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    // TODO: private final SearchService searchService;

    @Operation(summary = "통합 검색",
            description = "영화, TV, 게시글, 인물 등 여러 도메인의 검색 결과를 종합하여 반환합니다.")
    @GetMapping("/all")
    public void searchAll(
            @Parameter(description = "검색어") @RequestParam String query) {
        // TODO: 1. SearchService.searchAll(query) 호출
        // TODO: 2. 각 도메인별 검색 결과를 담은 SearchAllResponse (신규 DTO)로 변환하여 반환
    }

    @Operation(summary = "영화 검색")
    @GetMapping("/movies")
    public void searchMovies(@RequestParam String query) {
        // TODO: 1. MediaQueryService.searchMedia(query, "movie") 호출 (페이징)
        // TODO: 2. PagedResponse<MediaSummaryResponse> 형태로 반환
    }

    @Operation(summary = "TV 시리즈 검색")
    @GetMapping("/tv")
    public void searchTvSeries(@RequestParam String query) {
        // TODO: 1. MediaQueryService.searchMedia(query, "tv") 호출 (페이징)
        // TODO: 2. PagedResponse<MediaSummaryResponse> 형태로 반환
    }

    @Operation(summary = "게시글 검색")
    @GetMapping("/posts")
    public void searchPosts(@RequestParam String query) {
        // TODO: 1. PostService.searchPosts(query) 호출 (페이징)
        // TODO: 2. PagedResponse<PostSummaryResponse> 형태로 반환
    }

    @Operation(summary = "인물 검색")
    @GetMapping("/people")
    public void searchPeople(@RequestParam String query) {
        // TODO: 1. PeopleService.searchPeople(query) 호출 (페이징)
        // TODO: 2. PagedResponse<PersonSummaryResponse> (신규 DTO) 형태로 반환
    }

    @Operation(summary = "컬렉션 검색")
    @GetMapping("/collections")
    public void searchCollections(@RequestParam String query) {
        // TODO: 1. MediaService/CollectionService에서 컬렉션 검색 로직 호출 (페이징)
        // TODO: 2. PagedResponse<CollectionSummaryResponse> (신규 DTO) 형태로 반환
    }

    @Operation(summary = "키워드 기반 미디어 검색",
            description = "특정 키워드(태그)를 포함하는 미디어 목록을 조회합니다.")
    @GetMapping("/keywords")
    public void searchMediaByKeyword(@RequestParam String keyword) {
        // TODO: 1. MediaService에서 키워드 기반 검색 로직 호출 (페이징)
        // TODO: 2. PagedResponse<MediaSummaryResponse> 형태로 반환
    }

    @Operation(summary = "사용자 검색")
    @GetMapping("/users")
    public void searchUsers(@RequestParam String nickname) {
        // TODO: 1. UserService에서 닉네임으로 사용자 검색 (페이징)
        // TODO: 2. PagedResponse<UserProfileResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "자동완성 추천어 제공")
    @GetMapping("/suggest")
    public void getSearchSuggestions(@RequestParam String prefix) {
        // TODO: 1. Elasticsearch 또는 DB의 Full-Text Search를 사용하여 'prefix'로 시작하는 추천어 목록 조회
        // TODO: 2. SuggestionResponse (신규 DTO) 리스트로 반환
    }

    @Operation(summary = "인기 검색어 목록 조회")
    @GetMapping("/trends")
    public void getTrendingSearches() {
        // TODO: 1. Redis 또는 스케줄링된 DB 집계를 통해 최근 인기 검색어 목록 조회
        // TODO: 2. TrendingSearchResponse (신규 DTO) 리스트로 반환
    }
}