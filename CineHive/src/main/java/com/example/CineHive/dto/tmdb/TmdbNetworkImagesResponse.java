package com.example.CineHive.dto.tmdb;

import java.util.List;

/**
 * TMDB API의 방송사 이미지 목록 응답을 담는 DTO입니다.
 */
public record TmdbNetworkImagesResponse(
        Long id,
        List<TmdbLogoResponse> logos
) {}