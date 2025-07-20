package com.example.CineHive.dto.tmdb;

import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieResponse {
    private Long id;
    private String title;
    private String original_title;
    private String overview;
    private String release_date;
    private Double vote_average;
    private Integer vote_count;
    private Double popularity;
    private String poster_path;
    private String backdrop_path;
    private List<Long> genre_ids;
}
