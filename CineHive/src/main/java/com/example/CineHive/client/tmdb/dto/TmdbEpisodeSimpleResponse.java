package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbEpisodeSimpleResponse(
        long id,
        String name,
        String overview,
        @JsonProperty("episode_number") int episodeNumber,
        @JsonProperty("season_number") int seasonNumber,
        @JsonProperty("air_date") String airDate
) {}
