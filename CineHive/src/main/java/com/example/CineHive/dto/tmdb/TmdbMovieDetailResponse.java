package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * TMDB API의 영화 상세 정보 응답을 담는 DTO입니다.
 */
public record TmdbMovieDetailResponse(
        Long id,
        String title,
        @JsonProperty("original_title")
        String originalTitle,
        String overview,
        @JsonProperty("release_date")
        String releaseDate,
        @JsonProperty("vote_average")
        Double voteAverage,
        @JsonProperty("vote_count")
        Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path")
        String posterPath,
        @JsonProperty("backdrop_path")
        String backdropPath,
        List<TmdbGenreResponse> genres,
        TmdbCreditsResponse credits,
        TmdbVideosResponse videos,
        TmdbImagesResponse images,
        TmdbPagedResponse<TmdbMovieResponse> recommendations,
        TmdbPagedResponse<TmdbMovieResponse> similar,
        TmdbKeywordsResponse keywords,
        @JsonProperty("watch/providers")
        TmdbWatchProvidersResponse watchProviders
) {}