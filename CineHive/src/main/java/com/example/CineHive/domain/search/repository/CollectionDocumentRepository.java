package com.example.CineHive.domain.search.repository;

import com.example.CineHive.domain.search.document.CollectionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CollectionDocumentRepository extends ElasticsearchRepository<CollectionDocument, Long> {
}