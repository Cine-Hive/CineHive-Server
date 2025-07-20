package com.example.CineHive.dto.tmdb;

/**
 * TMDB API의 개별 장르 정보를 담는 DTO입니다.
 */
public record TmdbGenreResponse(
        Integer id,
        String name
) {}