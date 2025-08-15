package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB API의 Collection 상세 정보 응답을 담는 DTO입니다.
 */
public record TmdbCollectionDetailResponse(
        Long id,
        String name,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbMovieResponse> parts // 컬렉션에 속한 영화들
) {}