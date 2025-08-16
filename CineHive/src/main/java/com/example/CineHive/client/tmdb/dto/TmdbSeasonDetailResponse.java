package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * TMDB API의 Season 상세 정보 응답을 담는 DTO입니다.
 */
public record TmdbSeasonDetailResponse(
        Long id,
        String name,
        String overview,
        @JsonProperty("air_date") String airDate,
        @JsonProperty("season_number") int seasonNumber,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("vote_average") BigDecimal voteAverage,
        List<TmdbEpisodeDetailResponse> episodes,
        TmdbMediaCreditsResponse credits,
        TmdbImagesResponse images,
        TmdbVideosResponse videos,
        @JsonProperty("external_ids") TmdbExternalIdsResponse externalIds
) {}