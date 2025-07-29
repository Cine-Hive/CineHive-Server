package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * TMDB API의 TV 시리즈 상세 정보 응답을 담는 DTO입니다.
 */
public record TmdbTvSeriesDetailResponse(
        Long id,
        String name,
        @JsonProperty("original_name")
        String originalName,
        String overview,
        @JsonProperty("first_air_date")
        String firstAirDate,
        @JsonProperty("last_air_date")
        String lastAirDate,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("backdrop_path")
        String backdropPath,
        @JsonProperty("number_of_seasons")
        Integer numberOfSeasons,
        @JsonProperty("number_of_episodes")
        Integer numberOfEpisodes,
        String status,
        String type,
        List<TmdbGenreResponse> genres,
        TmdbCreditsResponse credits,
        TmdbVideosResponse videos,
        TmdbImagesResponse images,
        TmdbPagedResponse<TmdbTvSeriesResponse> recommendations,
        TmdbPagedResponse<TmdbTvSeriesResponse> similar,
        TmdbKeywordsResponse keywords,
        @JsonProperty("watch/providers")
        TmdbWatchProvidersResponse watchProviders
) {}