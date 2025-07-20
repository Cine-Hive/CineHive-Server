package com.example.CineHive.dto.tmdb;

import java.util.List;

/**
 * TMDB API의 이미지 목록(배경, 포스터) 응답을 담는 DTO입니다.
 */
public record TmdbImagesResponse(
        List<TmdbImageResponse> backdrops,
        List<TmdbImageResponse> posters
) {}