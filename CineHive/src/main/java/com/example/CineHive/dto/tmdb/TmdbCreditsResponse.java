package com.example.CineHive.dto.tmdb;

import java.util.List;

/**
 * TMDB API의 크레딧(출연진 및 제작진) 정보 전체를 담는 DTO입니다.
 */
public record TmdbCreditsResponse(
        List<TmdbCastResponse> cast,
        List<TmdbCrewResponse> crew
) {}