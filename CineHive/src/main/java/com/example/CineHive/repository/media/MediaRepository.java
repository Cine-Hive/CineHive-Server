package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface MediaRepository<T extends Media> extends JpaRepository<T, Long> {
    
    // 카테고리별 미디어 조회
    List<T> findByCategory(Media.MediaCategory category);
    
    // 카테고리별 페이징 조회
    Page<T> findByCategory(Media.MediaCategory category, Pageable pageable);
    
    // 제목으로 검색
    @Query("SELECT m FROM #{#entityName} m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<T> searchByTitleContainingIgnoreCase(@Param("keyword") String keyword);
    
    // 제목으로 페이지 검색
    @Query("SELECT m FROM #{#entityName} m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<T> searchByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
} 