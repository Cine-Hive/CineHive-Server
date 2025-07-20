package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB API의 TV 시리즈 요약 정보 응답을 담는 DTO입니다.
 */
public record TmdbTvSeriesResponse(
        Long id,
        String name,
        @JsonProperty("original_name")
        String originalName,
        String overview,
        @JsonProperty("first_air_date")
        String firstAirDate,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("backdrop_path")
        String backdropPath,
        @JsonProperty("genre_ids")
        List<Long> genreIds
) {}