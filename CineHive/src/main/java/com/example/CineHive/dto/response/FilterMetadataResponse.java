package com.example.CineHive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FilterMetadataResponse {
    private List<GenreOptionDto> movieGenres;
    private List<GenreOptionDto> tvGenres;
    private List<PlatformOptionDto> platforms;
    private List<SortOptionDto> sortOptions;
}