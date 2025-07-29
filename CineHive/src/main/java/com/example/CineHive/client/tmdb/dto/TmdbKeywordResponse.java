package com.example.CineHive.client.tmdb.dto;

/**
 * TMDB API의 개별 키워드 정보를 담는 DTO입니다.
 */
public record TmdbKeywordResponse(
        Long id,
        String name
) {}