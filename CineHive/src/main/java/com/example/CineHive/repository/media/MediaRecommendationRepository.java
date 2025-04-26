package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.MediaRecommendation;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MediaRecommendationRepository extends JpaRepository<MediaRecommendation, Long> {
    List<MediaRecommendation> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
    
    // 중복 추천 관계 확인
    boolean existsByMediaIdAndRecommendedMediaIdAndMediaTypeAndRecommendedMediaType(
            Long mediaId, Long recommendedMediaId, Media.MediaType mediaType, Media.MediaType recommendedMediaType);
    
    // 특정 미디어의 모든 추천 관계 삭제
    void deleteByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
    
    // 만료된 추천 관계 수 확인
    @Query("SELECT COUNT(m) FROM MediaRecommendation m WHERE m.expiresAt < :now")
    long countExpiredRecommendations(@Param("now") LocalDateTime now);
    
    // 만료된 추천 관계 삭제
    @Query("DELETE FROM MediaRecommendation m WHERE m.expiresAt < :now")
    void deleteExpiredRecommendations(@Param("now") LocalDateTime now);
    
    // 접근 빈도가 낮은 추천 관계 수 확인
    @Query("SELECT COUNT(m) FROM MediaRecommendation m WHERE m.accessCount < :threshold")
    long countByLowAccessCount(@Param("threshold") int threshold);
    
    // 접근 빈도가 낮은 추천 관계 삭제
    @Query("DELETE FROM MediaRecommendation m WHERE m.accessCount < :threshold")
    void deleteByLowAccessCount(@Param("threshold") int threshold);
    
    // 미디어 유형별 추천 관계 수
    long countByMediaType(Media.MediaType mediaType);
}