package com.example.CineHive.client.tmdb.dto;

import java.util.List;

/**
 * TMDB API의 국가별 시청 플랫폼 목록 응답을 담는 DTO입니다.
 */
public record TmdbCountryWatchProvidersResponse(
        String link,
        List<TmdbProviderResponse> flatrate, // 구독
        List<TmdbProviderResponse> rent,     // 대여
        List<TmdbProviderResponse> buy       // 구매
) {}