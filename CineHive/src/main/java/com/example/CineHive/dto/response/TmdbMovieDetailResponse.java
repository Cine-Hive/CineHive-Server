package com.example.CineHive.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbMovieDetailResponse {

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

    // 상세 정보에서는 name 포함된 genres도 같이 옴
    private List<TmdbGenreResponse> genres;
    private TmdbCreditsResponse credits;
    private TmdbVideosResponse videos;

    private TmdbImagesResponse images;
    private TmdbPagedResponse<TmdbMovieResponse> recommendations;
    private TmdbPagedResponse<TmdbMovieResponse> similar;
    private TmdbKeywordsResponse keywords;
    @JsonProperty("watch/providers")
    private TmdbWatchProvidersResponse watchProviders;
}