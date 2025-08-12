package com.example.CineHive.datasync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbExportItem(
        long id,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("original_name") String originalName,
        boolean adult,
        double popularity
) {}