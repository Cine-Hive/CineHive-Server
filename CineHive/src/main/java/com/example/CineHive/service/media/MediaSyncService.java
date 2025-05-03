package com.example.CineHive.service.media;

import com.example.CineHive.entity.media.Media;

public interface MediaSyncService {
    /**
     * 데이터 동기화 (TMDB API에서 데이터 가져오기)
     * 
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @param category 카테고리 (인기, 평점 높은, 현재 상영 중 등)
     */
    void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category);
    
    /**
     * 데이터 동기화 (TMDB API에서 데이터 가져오기)
     * 
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @param category 카테고리 (인기, 평점 높은, 현재 상영 중 등)
     * @param maxPages 가져올 최대 페이지 수
     */
    void syncMediaData(Media.MediaType mediaType, Media.MediaCategory category, int maxPages);
    
    /**
     * 특정 미디어 데이터 동기화 (TMDB API에서 데이터 가져오기)
     * 
     * @param mediaId TMDB 미디어 ID
     * @param mediaType 미디어 타입 (영화, TV, 애니메이션)
     * @return 동기화 성공 여부
     */
    boolean syncSingleMedia(Long mediaId, Media.MediaType mediaType);
} 