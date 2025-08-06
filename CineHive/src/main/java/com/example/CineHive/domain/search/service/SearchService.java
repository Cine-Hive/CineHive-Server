package com.example.CineHive.domain.search.service;

import com.example.CineHive.domain.search.dto.MediaSearchResponse;
import com.example.CineHive.domain.search.dto.PostSearchResponse;
import com.example.CineHive.global.dto.PageResponse;
import com.example.CineHive.global.dto.SliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 검색 기능을 총괄하는 서비스 인터페이스입니다.
 */
public interface SearchService {

    /**
     * Elasticsearch에 저장된 미디어 문서를 검색합니다.
     * @return 슬라이스된 미디어 검색 결과
     */
    SliceResponse<MediaSearchResponse> searchMedia(String query, Pageable pageable);

    /**
     * Elasticsearch에 저장된 게시글 문서를 검색합니다.
     * @return 슬라이스된 게시글 검색 결과
     */
    SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable);
}