package com.example.CineHive.client.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbReleaseDateResult(
        @JsonProperty("iso_3166_1") String countryCode,
        @JsonProperty("release_dates") List<TmdbReleaseDate> releaseDates
) {}
