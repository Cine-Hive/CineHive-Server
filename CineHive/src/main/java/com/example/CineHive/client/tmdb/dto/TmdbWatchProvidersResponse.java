package com.example.CineHive.client.tmdb.dto;

import java.util.Map;

/**
 * TMDB API의 시청 가능 플랫폼 전체 응답을 담는 DTO입니다.
 * 국가별로 시청 정보가 Map 형태로 제공됩니다.
 */
public record TmdbWatchProvidersResponse(
        Map<String, TmdbCountryWatchProvidersResponse> results
) {}