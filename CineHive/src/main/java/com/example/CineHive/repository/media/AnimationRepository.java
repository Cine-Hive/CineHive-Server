package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Animation;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationRepository extends JpaRepository<Animation, Long> {
    /**
     * 카테고리별 애니메이션 목록 조회
     */
    List<Animation> findAllByCategory(Media.MediaCategory category);
    
    /**
     * 카테고리별 애니메이션 목록 페이징 조회
     */
    Page<Animation> findAllByCategory(Media.MediaCategory category, Pageable pageable);
    
    /**
     * 제목 검색
     */
    List<Animation> findAllByTitleContainingIgnoreCase(String keyword);
} 