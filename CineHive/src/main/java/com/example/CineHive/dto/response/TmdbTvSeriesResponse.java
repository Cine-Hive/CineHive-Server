package com.example.CineHive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TmdbTvSeriesResponse {
    private Long id;
    private String name;
    private String original_name;
    private String overview;
    private String first_air_date;
    private Double vote_average;
    private Integer vote_count;
    private Double popularity;
    private String poster_path;
    private String backdrop_path;
    private List<Long> genre_ids;
    private Integer number_of_seasons;
    private Integer number_of_episodes;
}
