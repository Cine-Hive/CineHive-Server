package com.example.CineHive.domain.search.service;

import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.domain.search.dto.*;
import com.example.CineHive.global.dto.SliceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 검색 기능을 총괄하는 서비스 인터페이스입니다.
 */
public interface SearchService {

    /**
     * 여러 도메인의 검색 결과를 종합하여 반환합니다.
     * @param query 검색어
     * @return 각 도메인별 검색 결과를 포함하는 CompletableFuture
     */
    CompletableFuture<SearchAllResponse> searchAll(String query);

    /**
     * Elasticsearch에 저장된 미디어 문서를 검색합니다.
     * @param query 검색어
     * @param mediaType 검색할 미디어 타입 (movie, tv, null=전체)
     * @param pageable 페이징 정보
     * @return 슬라이스된 미디어 검색 결과
     */
    SliceResponse<MediaSearchResponse> searchMedia(String query, MediaType mediaType, Pageable pageable);

    /**
     * Elasticsearch에 저장된 게시글 문서를 검색합니다.
     * @return 슬라이스된 게시글 검색 결과
     */
    SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable);

    /**
     * Elasticsearch에 저장된 인물 문서를 검색합니다.
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 슬라이스된 인물 검색 결과
     */
    SliceResponse<PersonSearchResponse> searchPeople(String query, Pageable pageable);

    /**
     * 'Search-as-you-type'을 이용한 검색어 제안 (더 유연함)
     */
    List<String> getSearchSuggestions(String prefix);

    /**
     * 'Completion Suggester'를 이용한 자동완성 (더 빠름)
     */
    List<String> getAutocompletions(String prefix);

    /**
     * 인기 검색어 목록을 조회합니다.
     * @return 인기 검색어 상위 10개 목록
     */
    List<TrendingSearchResponse> getTrendingSearches();

    // --- 신규 또는 미구현 기능 ---

    /**
     * 닉네임으로 사용자를 검색합니다. (향후 구현)
     * @return 페이징된 사용자 검색 결과
     */
    SliceResponse<UserSearchResponse> searchUsers(String nickname, Pageable pageable);

    /**
     * 컬렉션을 검색합니다. (향후 구현)
     * @return 페이징된 컬렉션 검색 결과
     */
    SliceResponse<CollectionSearchResponse> searchCollections(String query, Pageable pageable);

    /**
     * 키워드로 미디어를 검색합니다. (향후 구현)
     * @return 페이징된 미디어 검색 결과
     */
    SliceResponse<MediaSearchResponse> searchMediaByKeyword(String keyword, Pageable pageable);
}