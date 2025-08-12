package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TmdbConfigurationResponse(
        Images images,
        @JsonProperty("change_keys") List<String> changeKeys
) {
    public record Images(
            @JsonProperty("secure_base_url")
            String secureBaseUrl,
            @JsonProperty("base_url")
            String baseUrl,
            @JsonProperty("poster_sizes")
            List<String> posterSizes,
            @JsonProperty("backdrop_sizes")
            List<String> backdropSizes,
            @JsonProperty("still_sizes")
            List<String> stillSizes,
            @JsonProperty("profile_sizes")
            List<String> profileSizes,
            @JsonProperty("logo_sizes")
            List<String> logoSizes
    ) {}
}