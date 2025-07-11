package com.example.CineHive.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class TmdbMultiSearchResponse {
    private Long id;
    private String media_type; // "movie" or "tv"

    // 영화 필드
    private String title;
    private String original_title;
    private String release_date;

    // TV 시리즈 필드
    private String name;
    private String original_name;
    private String first_air_date;

    // 공통 필드
    private String overview;
    private Double vote_average;
    private Integer vote_count;
    private Double popularity;
    private String poster_path;
    private String backdrop_path;
    private List<Long> genre_ids;
}