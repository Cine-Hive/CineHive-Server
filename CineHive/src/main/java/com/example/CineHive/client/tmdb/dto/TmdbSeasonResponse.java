package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * TV 시리즈 상세 정보 응답에 포함된 개별 시즌의 요약 정보를 담는 DTO입니다.
 */
public record TmdbSeasonResponse(
        long id,
        @JsonProperty("air_date") String airDate,
        @JsonProperty("episode_count") int episodeCount,
        String name,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("season_number") int seasonNumber,
        @JsonProperty("vote_average") BigDecimal voteAverage
) {}