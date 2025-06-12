package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbCountryWatchProvidersResponse {
    private String link;
    private List<TmdbProviderResponse> flatrate;
    private List<TmdbProviderResponse> rent;
    private List<TmdbProviderResponse> buy;
}