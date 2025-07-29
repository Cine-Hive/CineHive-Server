package com.example.CineHive.client.tmdb.dto;

import java.util.List;

/**
 * TMDB API의 장르 목록 응답을 담는 DTO입니다.
 */
public record TmdbGenresResponse(
        List<TmdbGenreResponse> genres
) {}