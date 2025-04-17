package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.entity.media.Media;

import java.util.List;
import java.util.Map;

public interface MediaService {
    // 기본 미디어 조회
    MediaDto.MediaItemDto getMediaById(Media.MediaType mediaType, Long id);
    
    // 검색
    MediaDto searchMedia(Media.MediaType mediaType, String query, int page);
    
    // 카테고리별 미디어 목록
    MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page);
    
    // 유사 미디어 조회
    MediaDto getSimilarMedia(Media.MediaType mediaType, Long id, int page);
    
    // 출연/제작진 정보
    MediaDto.MediaItemDto getMediaWithCredits(Media.MediaType mediaType, Long id);
    
    // 비디오 정보
    List<MediaDto.VideoDto> getMediaVideos(Media.MediaType mediaType, Long id);
    
    // 애니메이션 전용 (장르 필터링)
    MediaDto getAnimationsByCategory(Media.MediaCategory category, int page);
    MediaDto searchAnimations(String query, int page);
    
    // 데이터 동기화 (TMDB API에서 데이터 가져오기)
    void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category);

    // 관리자 기능
    // 특정 미디어의 추천 정보 강제 갱신
    void refreshRecommendations(Media.MediaType mediaType, Long mediaId);
    
    // 특정 미디어의 추천 정보 삭제
    void deleteRecommendations(Media.MediaType mediaType, Long mediaId);
    
    // 추천 정보 통계 조회 (만료 예정 수, 접근 빈도 낮은 수 등)
    Map<String, Object> getRecommendationStats();
    
    // 접근 빈도 기준 조정
    void updateAccessCountThreshold(int threshold);
    
    // 만료 기간 조정
    void updateExpiryDays(int days);
} 