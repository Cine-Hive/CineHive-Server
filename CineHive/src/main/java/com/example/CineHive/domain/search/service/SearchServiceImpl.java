package com.example.CineHive.domain.search.service;

import com.example.CineHive.domain.search.document.MediaDocument;
import com.example.CineHive.domain.search.document.PostDocument;
import com.example.CineHive.domain.search.dto.MediaSearchResponse;
import com.example.CineHive.domain.search.dto.PostSearchResponse;
import com.example.CineHive.domain.search.repository.MediaDocumentRepository;
import com.example.CineHive.domain.search.repository.PostDocumentRepository;
import com.example.CineHive.global.dto.PageResponse;
import com.example.CineHive.global.dto.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final MediaDocumentRepository mediaDocumentRepository;
    private final PostDocumentRepository postDocumentRepository;

    @Override
    public SliceResponse<MediaSearchResponse> searchMedia(String query, Pageable pageable) {
        log.debug("Elasticsearch 미디어 검색을 시작합니다. Query: {}", query);
        Page<MediaDocument> resultPage = mediaDocumentRepository.findByTitleContainsOrOverviewContains(query, query, pageable);
        return SliceResponse.from(resultPage, MediaSearchResponse::from);
    }

    @Override
    public SliceResponse<PostSearchResponse> searchPosts(String query, Pageable pageable) {
        log.debug("Elasticsearch 게시글 검색을 시작합니다. Query: {}", query);
        Page<PostDocument> resultPage = postDocumentRepository.findByTitleContainsOrContentContains(query, query, pageable);
        return SliceResponse.from(resultPage, PostSearchResponse::from);
    }
}