package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * TMDB API의 Episode 상세 정보 응답을 담는 DTO입니다.
 */
public record TmdbEpisodeDetailResponse(
        Long id,
        String name,
        String overview,
        @JsonProperty("air_date") String airDate,
        @JsonProperty("episode_number") int episodeNumber,
        @JsonProperty("season_number") int seasonNumber,
        @JsonProperty("still_path") String stillPath,
        @JsonProperty("vote_average") BigDecimal voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        Integer runtime,
        @JsonProperty("production_code") String productionCode,
        @JsonProperty("guest_stars") List<TmdbMediaCastResponse> guestStars,
        TmdbMediaCreditsResponse credits,
        TmdbImagesResponse images,
        TmdbVideosResponse videos
) {}