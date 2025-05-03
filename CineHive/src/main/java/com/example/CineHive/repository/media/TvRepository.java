package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Media;
import com.example.CineHive.entity.media.Tv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TvRepository extends JpaRepository<Tv, Long> {
    /**
     * 카테고리별 TV 프로그램 목록 조회
     */
    List<Tv> findAllByCategory(Media.MediaCategory category);
    
    /**
     * 카테고리별 TV 프로그램 목록 페이징 조회
     */
    Page<Tv> findAllByCategory(Media.MediaCategory category, Pageable pageable);
    
    /**
     * 제목 검색
     */
    List<Tv> findAllByTitleContainingIgnoreCase(String keyword);
} 