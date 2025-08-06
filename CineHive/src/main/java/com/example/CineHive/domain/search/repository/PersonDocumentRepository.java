package com.example.CineHive.domain.search.repository;

import com.example.CineHive.domain.search.document.PersonDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PersonDocumentRepository extends ElasticsearchRepository<PersonDocument, Long> {
    /**
     * 이름에 주어진 키워드가 포함된 인물 문서를 검색합니다.
     */
    Page<PersonDocument> findByNameContains(String name, Pageable pageable);
}