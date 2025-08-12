package com.example.CineHive.client.tmdb.dto;

import java.util.List;

public record TmdbTranslationsResponse(
        List<TmdbTranslation> translations
) {}