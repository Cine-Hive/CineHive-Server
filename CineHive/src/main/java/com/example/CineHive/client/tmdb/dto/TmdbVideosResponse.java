package com.example.CineHive.client.tmdb.dto;

import java.util.List;

/**
 * TMDB API의 비디오(예고편 등) 목록 응답을 담는 DTO입니다.
 */
public record TmdbVideosResponse(
        List<TmdbVideoResponse> results
) {}