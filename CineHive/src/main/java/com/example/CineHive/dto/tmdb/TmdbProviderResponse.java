package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TMDB API의 개별 시청 플랫폼(Provider) 정보를 담는 DTO입니다.
 */
public record TmdbProviderResponse(
        @JsonProperty("provider_id")
        Long providerId,
        @JsonProperty("provider_name")
        String providerName,
        @JsonProperty("logo_path")
        String logoPath,
        @JsonProperty("display_priority")
        Integer displayPriority
) {}