package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record TmdbMovieDetailResponse(
        long id,
        String title,
        @JsonProperty("original_title") String originalTitle,
        String overview,
        String tagline,
        @JsonProperty("release_date") String releaseDate,
        Integer runtime,
        String status,
        Long budget,
        Long revenue,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("vote_count") Integer voteCount,
        Double popularity,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbGenreResponse> genres,
        TmdbMediaCreditsResponse credits,
        boolean adult,
        TmdbVideosResponse videos,
        TmdbImagesResponse images,
        TmdbPagedResponse<TmdbMovieResponse> recommendations,
        TmdbPagedResponse<TmdbMovieResponse> similar,
        TmdbKeywordsResponse keywords,
        @JsonProperty("watch/providers") TmdbWatchProvidersResponse watchProviders,
        @JsonProperty("belongs_to_collection") TmdbCollectionResponse collection,
        @JsonProperty("production_companies") List<TmdbProductionCompany> productionCompanies,
        @JsonProperty("release_dates") TmdbReleaseDatesResponse releaseDates,
        TmdbTranslationsResponse translations
) {}