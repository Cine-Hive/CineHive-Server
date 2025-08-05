package com.example.CineHive.domain.media.service;

import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.domain.review.dto.RatingStats;

/**
 * 미디어 정보 조회, 생성, 업데이트 등 미디어 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface MediaService {

    /**
     * TMDB ID와 미디어 타입을 기준으로 DB에서 미디어를 찾거나,
     * 존재하지 않을 경우 TMDB API를 통해 새로 생성하여 저장한 후 반환합니다.
     *
     * @param tmdbId    미디어의 TMDB ID
     * @param mediaType 미디어 타입 (MOVIE 또는 TV)
     * @return DB에 저장된 Media 엔티티
     */
    Media findOrCreateMedia(Integer tmdbId, MediaType mediaType);

    /**
     * 특정 미디어의 평균 별점과 리뷰 수를 다시 계산하여 갱신합니다.
     *
     * @param media 평점을 갱신할 Media 엔티티
     * @param stats 새로운 평점과 리뷰 수 정보를 담은 DTO
     */
    void updateMediaRating(Media media, RatingStats stats);
}