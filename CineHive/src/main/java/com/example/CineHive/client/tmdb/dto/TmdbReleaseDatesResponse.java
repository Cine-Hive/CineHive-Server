package com.example.CineHive.client.tmdb.dto;

import java.util.List;

public record TmdbReleaseDatesResponse(
        List<TmdbReleaseDateResult> results
) {}