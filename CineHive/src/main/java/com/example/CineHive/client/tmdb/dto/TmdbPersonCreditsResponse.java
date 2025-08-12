package com.example.CineHive.client.tmdb.dto;

import java.util.List;

public record TmdbPersonCreditsResponse(
        List<TmdbPersonCreditResponse> cast,
        List<TmdbPersonCreditResponse> crew
) {}