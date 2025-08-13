package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
}