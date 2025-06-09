package com.example.CineHive.client;

import com.example.CineHive.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private WebClient tmdbWebClient() {
        return webClientBuilder.baseUrl(tmdbBaseUrl).build();
    }

    private static final String LANGUAGE = "ko-KR";
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String QUERY_PARAM = "query";

    private <T> List<T> getPagedResponse(String path, int page, ParameterizedTypeReference<TmdbPagedResponse<T>> typeRef) {
        TmdbPagedResponse<T> response = tmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam(API_KEY_PARAM, apiKey)
                        .queryParam(LANGUAGE_PARAM, LANGUAGE)
                        .queryParam(PAGE_PARAM, page)
                        .build())
                .retrieve()
                .bodyToMono(typeRef)
                .block();

        return response != null ? response.getResults() : Collections.emptyList();
    }

    // 기존 메서드들
    public List<TmdbMovieResponse> getPopularMovies(int page) {
        return getPagedResponse("/movie/popular", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {});
    }

    public List<TmdbTvSeriesResponse> getTopRatedTvSeries(int page) {
        return getPagedResponse("/tv/top_rated", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {});
    }

    public List<TmdbMovieResponse> getUpcomingMovies(int page) {
        return getPagedResponse("/movie/upcoming", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {});
    }

    public List<TmdbTvSeriesResponse> getOnTheAirTvSeries(int page) {
        return getPagedResponse("/tv/on_the_air", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {});
    }

    public List<TmdbTvSeriesResponse> getPopularTvSeries(int page) {
        return getPagedResponse("/tv/popular", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {});
    }

    public List<TmdbMovieResponse> getTopRatedMovies(int page) {
        return getPagedResponse("/movie/top_rated", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {});
    }

    public List<TmdbMovieResponse> getNowPlayingMovies(int page) {
        return getPagedResponse("/movie/now_playing", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {});
    }

    public List<TmdbTvSeriesResponse> getAiringTodayTvSeries(int page) {
        return getPagedResponse("/tv/airing_today", page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {});
    }

    /**
     * 영화 상세 정보 조회
     */
    public TmdbMovieDetailResponse getMovieDetail(Long movieId) {
        return tmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movieId}")
                        .queryParam(API_KEY_PARAM, apiKey)
                        .queryParam(LANGUAGE_PARAM, LANGUAGE)
                        .queryParam("include_image_language", "ko,null")
                        .queryParam("append_to_response", "credits,videos,images,recommendations,similar,keywords,watch/providers")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMovieDetailResponse.class)
                .block();
    }

    /**
     * TV 시리즈 상세 정보 조회
     */
    public TmdbTvSeriesDetailResponse getTvSeriesDetail(Long tvId) {
        return tmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tvId}")
                        .queryParam(API_KEY_PARAM, apiKey)
                        .queryParam(LANGUAGE_PARAM, LANGUAGE)
                        .queryParam("include_image_language", "ko,null")
                        .queryParam("append_to_response", "credits,videos,images,recommendations,similar,keywords,watch/providers")
                        .build(tvId))
                .retrieve()
                .bodyToMono(TmdbTvSeriesDetailResponse.class)
                .block();
    }

    /**
     * 멀티 검색 (영화 + TV 시리즈)
     */
    public List<TmdbMultiSearchResponse> searchMulti(String query, int page) {
        TmdbPagedResponse<TmdbMultiSearchResponse> response = tmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/multi")
                        .queryParam(API_KEY_PARAM, apiKey)
                        .queryParam(LANGUAGE_PARAM, LANGUAGE)
                        .queryParam(PAGE_PARAM, page)
                        .queryParam(QUERY_PARAM, query)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<TmdbPagedResponse<TmdbMultiSearchResponse>>() {})
                .block();

        return response != null ? response.getResults() : Collections.emptyList();
    }

    /**
     * 영화 검색
     */
    public List<TmdbMovieResponse> searchMovies(String query, int page) {
        return getSearchPagedResponse("/search/movie", query, page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {});
    }

    /**
     * TV 시리즈 검색
     */
    public List<TmdbTvSeriesResponse> searchTvSeries(String query, int page) {
        return getSearchPagedResponse("/search/tv", query, page,
                new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {});
    }

    private <T> List<T> getSearchPagedResponse(String path, String query, int page,
                                               ParameterizedTypeReference<TmdbPagedResponse<T>> typeRef) {
        TmdbPagedResponse<T> response = tmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam(API_KEY_PARAM, apiKey)
                        .queryParam(LANGUAGE_PARAM, LANGUAGE)
                        .queryParam(PAGE_PARAM, page)
                        .queryParam(QUERY_PARAM, query)
                        .build())
                .retrieve()
                .bodyToMono(typeRef)
                .block();

        return response != null ? response.getResults() : Collections.emptyList();
    }
}