package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbPersonCreditsResponse(
        @JsonProperty("cast")
        List<TmdbPersonCreditResponse> cast,
        @JsonProperty("crew")
        List<TmdbPersonCreditResponse> crew
) {
}