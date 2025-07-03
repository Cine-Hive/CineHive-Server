package com.example.CineHive.dto.response;

public record SortOptionDto(
        String value, // e.g., "popularity.desc"
        String label  // e.g., "인기순"
) {}