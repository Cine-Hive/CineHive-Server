package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbTranslation(
        @JsonProperty("iso_639_1") String languageCode,
        @JsonProperty("iso_3166_1") String countryCode,
        String name,
        @JsonProperty("english_name") String englishName,
        TranslationData data
) {}
