package com.example.CineHive.dto.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    MOVIE("movie"),
    TV("tv");

    private final String value;

    public static MediaType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Media type cannot be null or empty");
        }

        String normalizedValue = value.toLowerCase().trim();
        for (MediaType type : MediaType.values()) {
            if (type.getValue().equals(normalizedValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid media type: " + value + ". Supported types: movie, tv");
    }

    public boolean isMovie() {
        return this == MOVIE;
    }

    public boolean isTv() {
        return this == TV;
    }
}
