package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbChangesResponse(
        @JsonProperty("results")
        List<TmdbChangeItemResponse> results,
        @JsonProperty("page")
        int page,
        @JsonProperty("total_pages")
        int totalPages
) {}