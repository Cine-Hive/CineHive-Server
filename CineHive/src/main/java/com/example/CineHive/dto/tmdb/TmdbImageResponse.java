package com.example.CineHive.dto.tmdb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TmdbImageResponse {
    private Double aspect_ratio;
    private Integer height;
    private String iso_639_1;
    private String file_path;
    private Double vote_average;
    private Integer vote_count;
    private Integer width;
}