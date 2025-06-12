package com.example.CineHive.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbProviderResponse {
    private Long provider_id;
    private String provider_name;
    private String logo_path;
    private Integer display_priority;
}