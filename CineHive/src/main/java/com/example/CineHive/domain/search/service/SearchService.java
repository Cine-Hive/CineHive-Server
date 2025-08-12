package com.example.CineHive.domain.search.service;

import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.domain.search.dto.*;
import com.example.CineHive.global.dto.SliceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 검색 서비스 인터페이스
 */
public interface SearchService {

    /**
     * 통합 검색을 수행합니다.
     * @param query 검색어
     * @return 통합 검색 결과
     */
    CompletableFuture<SearchAllResponse> searchAll(String query);

    /**
     * 미디어(영화/TV) 검색을 수행합니다.
     * @param query 검색어
     * @param mediaType 미디어 타입 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 미디어 검색 결과
     */
    SliceResponse<MediaSearchResponse> searchMedia(String query, MediaType mediaType, Pageable pageable);

    /**
     * 게시글 검색을 수행합니다.
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 게시글 검색 결과
     */
    SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable);

    /**
     * 인물 검색을 수행합니다.
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 인물 검색 결과
     */
    SliceResponse<PersonSearchResponse> searchPeople(String query, Pageable pageable);

    /**
     * 사용자 검색을 수행합니다.
     * @param nickname 닉네임
     * @param pageable 페이징 정보
     * @return 사용자 검색 결과
     */
    SliceResponse<UserSearchResponse> searchUsers(String nickname, Pageable pageable);

    /**
     * 컬렉션 검색을 수행합니다.
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 컬렉션 검색 결과
     */
    SliceResponse<CollectionSearchResponse> searchCollections(String query, Pageable pageable);

    /**
     * 키워드 기반 미디어 검색을 수행합니다.
     * @param keyword 키워드
     * @param pageable 페이징 정보
     * @return 미디어 검색 결과
     */
    SliceResponse<MediaSearchResponse> searchMediaByKeyword(String keyword, Pageable pageable);

    /**
     * 검색 제안어를 가져옵니다.
     * @param prefix 검색어 접두사
     * @return 제안어 목록
     */
    List<String> getSearchSuggestions(String prefix);

    /**
     * 자동완성어를 가져옵니다.
     * @param prefix 검색어 접두사
     * @return 자동완성어 목록
     */
    List<String> getAutocompletions(String prefix);

    /**
     * 인기 검색어를 가져옵니다.
     * @return 인기 검색어 목록
     */
    List<TrendingSearchResponse> getTrendingSearches();
}