package com.example.CineHive.domain.meta.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 클라이언트 UI(검색 필터, 정렬 옵션 등)에 필요한 각종 메타데이터를 조회하는 API 컨트롤러입니다.
 */
@Tag(name = "Metadata Controller", description = "UI 구성을 위한 메타데이터 API")
@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
public class MetadataController {

    // TODO: private final MetadataService metadataService;

    @Operation(summary = "장르 목록 조회")
    @GetMapping("/genres")
    public void getGenres(
            @Parameter(description = "필터링할 미디어 타입 (movie 또는 tv). 생략 시 전체 장르 조회")
            @RequestParam(required = false) String type) {
        // TODO: 1. MetadataService.getGenres(type) 호출
        // TODO: 2. Genre enum을 사용하여 필터링된 GenreOption 리스트를 반환
    }

    @Operation(summary = "OTT 플랫폼 목록 조회")
    @GetMapping("/platforms")
    public void getPlatforms() {
        // TODO: 1. MetadataService.getPlatforms() 호출
        // TODO: 2. Platform enum 또는 PlatformMetadataService를 사용하여 PlatformOption 리스트를 반환
    }

    @Operation(summary = "정렬 옵션 목록 조회")
    @GetMapping("/sort-options")
    public void getSortOptions() {
        // TODO: 1. MetadataService.getSortOptions() 호출
        // TODO: 2. SortOption 리스트를 반환
    }

    @Operation(summary = "콘텐츠 등급 목록 조회",
            description = "영상물 등급(전체, 12+, 15+ 등) 목록을 조회합니다.")
    @GetMapping("/ratings")
    public void getContentRatings() {
        // TODO: 1. TMDB API의 'certifications' 엔드포인트 호출 또는 자체 정의 목록 반환
        // TODO: 2. RatingOption (신규 DTO) 리스트로 변환하여 반환
    }

    @Operation(summary = "제작 국가 목록 조회")
    @GetMapping("/countries")
    public void getCountries() {
        // TODO: 1. TMDB API의 'countries' 엔드포인트 호출
        // TODO: 2. CountryOption (신규 DTO) 리스트로 변환하여 반환
    }

    @Operation(summary = "인기 키워드/태그 목록 조회")
    @GetMapping("/keywords/popular")
    public void getPopularKeywords() {
        // TODO: 1. TMDB API 또는 자체 집계 데이터를 통해 인기 키워드 목록 조회
        // TODO: 2. KeywordInfo 리스트로 반환
    }

    @Operation(summary = "콘텐츠 상태 목록 조회",
            description = "TV 시리즈의 방영 상태(방영중, 종영 등) 목록을 조회합니다.")
    @GetMapping("/statuses")
    public void getContentStatuses() {
        // TODO: 1. 자체적으로 필요한 상태 목록을 Enum 또는 DB로 정의
        // TODO: 2. StatusOption (신규 DTO) 리스트로 반환
    }

    @Operation(summary = "주요 제작사/네트워크 목록 조회")
    @GetMapping("/companies")
    public void getMajorCompanies() {
        // TODO: 1. 자체적으로 선별한 주요 제작사/네트워크 목록을 Enum 또는 DB로 정의
        // TODO: 2. CompanyOption (신규 DTO) 리스트로 반환
    }

    @Operation(summary = "게시글 카테고리 목록 조회")
    @GetMapping("/post-categories")
    public void getPostCategories() {
        // TODO: 1. CategoryService에서 모든 게시글 카테고리 목록 조회
        // TODO: 2. CategoryOption (신규 DTO) 리스트로 변환하여 반환
    }
}