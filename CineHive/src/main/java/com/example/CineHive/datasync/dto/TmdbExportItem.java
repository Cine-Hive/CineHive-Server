package com.example.CineHive.datasync.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbExportItem(
        long id,
        @JsonProperty("original_title") String originalTitle,
        @JsonProperty("original_name") String originalName,
        boolean adult,
        double popularity,
        @JsonProperty("video") Boolean video
) {
    public Integer getPriority() {
        if (popularity > 100.0) {
            return 10;
        }
        if (popularity > 50.0) {
            return 5;
        }
        return 0;
    }

    public boolean shouldSkip() {
        return adult;
    }
}