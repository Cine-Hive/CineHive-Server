package com.example.CineHive.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbTvSeriesDetailResponse {
    private Long id;
    private String name;
    private String original_name;
    private String overview;
    private String first_air_date;
    private String last_air_date;
    private Double vote_average;
    private Integer vote_count;
    private Double popularity;
    private String poster_path;
    private String backdrop_path;
    private List<Long> genre_ids;
    private Integer number_of_seasons;
    private Integer number_of_episodes;
    private String status;
    private String type;

    // 상세 정보에서는 name 포함된 genres도 같이 옴
    private List<TmdbGenreResponse> genres;
    private TmdbCreditsResponse credits;
    private TmdbVideosResponse videos;
    private TmdbImagesResponse images;
    private TmdbPagedResponse<TmdbTvSeriesResponse> recommendations;
    private TmdbPagedResponse<TmdbTvSeriesResponse> similar;
    private TmdbKeywordsResponse keywords;
    @JsonProperty("watch/providers")
    private TmdbWatchProvidersResponse watchProviders;
}