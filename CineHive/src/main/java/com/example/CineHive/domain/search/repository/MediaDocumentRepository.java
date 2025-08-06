package com.example.CineHive.domain.search.repository;

import com.example.CineHive.domain.search.document.MediaDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MediaDocumentRepository extends ElasticsearchRepository<MediaDocument, Long> {

    /**
     * 제목 또는 줄거리에 주어진 키워드가 포함된 미디어 문서를 검색합니다.
     * @param title 제목 검색 키워드
     * @param overview 줄거리 검색 키워드
     * @param pageable 페이징 정보
     * @return 페이징된 미디어 문서
     */
    Page<MediaDocument> findByTitleContainsOrOverviewContains(String title, String overview, Pageable pageable);
}