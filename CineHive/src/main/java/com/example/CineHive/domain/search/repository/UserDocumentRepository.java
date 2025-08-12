package com.example.CineHive.domain.search.repository;

import com.example.CineHive.domain.search.document.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserDocumentRepository extends ElasticsearchRepository<UserDocument, Long> {
}