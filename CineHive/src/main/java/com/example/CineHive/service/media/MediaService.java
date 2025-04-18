package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.MediaDetailsDto;
import com.example.CineHive.dto.media.MediaDto;
import com.example.CineHive.dto.media.MediaItemDto;
import com.example.CineHive.dto.media.VideoDto;
import com.example.CineHive.dto.media.MediaCreditsDto;
import com.example.CineHive.entity.media.Media;

import java.util.List;
import java.util.Map;

public interface MediaService {
    // 기본 미디어 조회
    MediaItemDto getMediaById(Media.MediaType mediaType, Long id);
    
    // 검색
    MediaDto searchMedia(Media.MediaType mediaType, String query, int page);
    
    // 카테고리별 미디어 목록
    MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page);
    
    // 카테고리별 미디어 목록 (정렬 옵션 포함)
    MediaDto getMediaByCategory(Media.MediaType mediaType, Media.MediaCategory category, int page, String sortBy);
    
    // 유사 미디어 조회
    MediaDto getSimilarMedia(Media.MediaType mediaType, Long id, int page);
    
    // 출연/제작진 정보
    MediaCreditsDto getMediaWithCredits(Media.MediaType mediaType, Long id);
    
    // 비디오 정보
    List<VideoDto> getMediaVideos(Media.MediaType mediaType, Long id);
    
    // 미디어 통합 상세 정보 조회 (기본 정보 + 출연/제작진 + 비디오 + 유사 미디어)
    MediaDetailsDto getMediaDetails(Media.MediaType mediaType, Long id);
    
    // 애니메이션 전용 (장르 필터링)
    MediaDto getAnimationsByCategory(Media.MediaCategory category, int page);
    MediaDto getAnimationsByCategory(Media.MediaCategory category, int page, String sortBy);
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