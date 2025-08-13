package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record TmdbTvSeriesDetailResponse(
        long id,
        String name,
        @JsonProperty("original_name") String originalName,
        String overview,
        String tagline,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("last_air_date") String lastAirDate,
        @JsonProperty("number_of_seasons") int numberOfSeasons,
        @JsonProperty("number_of_episodes") int numberOfEpisodes,
        @JsonProperty("in_production") boolean inProduction,
        String status,
        String type,
        BigDecimal popularity,
        @JsonProperty("vote_average") BigDecimal voteAverage,
        @JsonProperty("vote_count") int voteCount,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbGenreResponse> genres,
        @JsonProperty("aggregate_credits") TmdbMediaCreditsResponse aggregateCredits, // aggregate_credits가 더 정확
        TmdbKeywordsResponse keywords,
        List<TmdbSeasonResponse> seasons,
        List<TmdbNetworkImagesResponse> networks,
        @JsonProperty("production_companies") List<TmdbProductionCompanyResponse> productionCompanies,
        @JsonProperty("created_by") List<TmdbCreatorResponse> createdBy,
        @JsonProperty("last_episode_to_air") TmdbEpisodeSimpleResponse lastEpisodeToAir,
        @JsonProperty("next_episode_to_air") TmdbEpisodeSimpleResponse nextEpisodeToAir
) {}