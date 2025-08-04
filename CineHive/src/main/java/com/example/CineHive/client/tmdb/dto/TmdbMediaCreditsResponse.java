package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbMediaCreditsResponse(
        @JsonProperty("id")
        Long mediaId,
        @JsonProperty("cast")
        List<TmdbMediaCastResponse> cast,
        @JsonProperty("crew")
        List<TmdbMediaCrewResponse> crew
) {
}