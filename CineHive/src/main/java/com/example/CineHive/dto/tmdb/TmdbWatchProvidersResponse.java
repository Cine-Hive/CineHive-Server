package com.example.CineHive.dto.tmdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbWatchProvidersResponse {
    private Map<String, TmdbCountryWatchProvidersResponse> results;
}