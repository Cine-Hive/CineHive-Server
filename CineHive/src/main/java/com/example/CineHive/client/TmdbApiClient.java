package com.example.CineHive.client;

import com.example.CineHive.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
        try {
            // page 값 검증 (1-1000 범위)
            int validatedPage = Math.max(1, Math.min(page, 1000));

            log.debug("Requesting TMDB API: {} with page: {}", path, validatedPage);

            TmdbPagedResponse<T> response = tmdbWebClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam(API_KEY_PARAM, apiKey)
                            .queryParam(LANGUAGE_PARAM, LANGUAGE)
                            .queryParam(PAGE_PARAM, validatedPage)
                            .build())
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();

            return response != null ? response.getResults() : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("TMDB API request failed for path: {}, page: {}, status: {}, body: {}",
                    path, page, e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode().is4xxClientError()) {
                // 클라이언트 오류의 경우 상세 로깅
                log.error("Client error details - API Key present: {}, Language: {}, Page: {}",
                        apiKey != null && !apiKey.isEmpty(), LANGUAGE, page);
            }

            throw new RuntimeException("TMDB API 요청 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during TMDB API request for path: {}, page: {}", path, page, e);
            throw new RuntimeException("TMDB API 요청 중 예상치 못한 오류 발생", e);
        }
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
     * 애니메이션 영화 발견 (인기순)
     */
    public List<TmdbMovieResponse> discoverAnimationMovies(int page, String sortBy) {
        return getDiscoverMoviesResponse(page, sortBy, "16"); // 16 = Animation genre
    }

    /**
     * 애니메이션 TV 시리즈 발견 (인기순)
     */
    public List<TmdbTvSeriesResponse> discoverAnimationTvSeries(int page, String sortBy) {
        return getDiscoverTvResponse(page, sortBy, "16"); // 16 = Animation genre
    }

    /**
     * 영화 발견 API (장르별 필터링 및 정렬)
     */
    public List<TmdbMovieResponse> discoverMovies(int page, String sortBy, String genreIds) {
        return getDiscoverMoviesResponse(page, sortBy, genreIds);
    }

    /**
     * TV 시리즈 발견 API (장르별 필터링 및 정렬)
     */
    public List<TmdbTvSeriesResponse> discoverTvSeries(int page, String sortBy, String genreIds) {
        return getDiscoverTvResponse(page, sortBy, genreIds);
    }

    private List<TmdbMovieResponse> getDiscoverMoviesResponse(int page, String sortBy, String genreIds) {
        try {
            // page 값 검증
            int validatedPage = Math.max(1, Math.min(page, 1000));

            log.debug("Discovering movies with genres: {}, sortBy: {}, page: {}", genreIds, sortBy, validatedPage);

            TmdbPagedResponse<TmdbMovieResponse> response = tmdbWebClient()
                    .get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/discover/movie")
                                .queryParam(API_KEY_PARAM, apiKey)
                                .queryParam(LANGUAGE_PARAM, LANGUAGE)
                                .queryParam(PAGE_PARAM, validatedPage);

                        if (sortBy != null && !sortBy.isEmpty()) {
                            builder.queryParam("sort_by", sortBy);
                        }

                        if (genreIds != null && !genreIds.isEmpty()) {
                            builder.queryParam("with_genres", genreIds);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<TmdbPagedResponse<TmdbMovieResponse>>() {})
                    .block();

            return response != null ? response.getResults() : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("Discover movies failed for genres: {}, sortBy: {}, page: {}, status: {}, body: {}",
                    genreIds, sortBy, page, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("영화 발견 요청 실패: " + e.getMessage(), e);
        }
    }

    private List<TmdbTvSeriesResponse> getDiscoverTvResponse(int page, String sortBy, String genreIds) {
        try {
            // page 값 검증
            int validatedPage = Math.max(1, Math.min(page, 1000));

            log.debug("Discovering TV series with genres: {}, sortBy: {}, page: {}", genreIds, sortBy, validatedPage);

            TmdbPagedResponse<TmdbTvSeriesResponse> response = tmdbWebClient()
                    .get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/discover/tv")
                                .queryParam(API_KEY_PARAM, apiKey)
                                .queryParam(LANGUAGE_PARAM, LANGUAGE)
                                .queryParam(PAGE_PARAM, validatedPage);

                        if (sortBy != null && !sortBy.isEmpty()) {
                            builder.queryParam("sort_by", sortBy);
                        }

                        if (genreIds != null && !genreIds.isEmpty()) {
                            builder.queryParam("with_genres", genreIds);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<TmdbPagedResponse<TmdbTvSeriesResponse>>() {})
                    .block();

            return response != null ? response.getResults() : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("Discover TV series failed for genres: {}, sortBy: {}, page: {}, status: {}, body: {}",
                    genreIds, sortBy, page, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("TV 시리즈 발견 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 영화 상세 정보 조회
     */
    public TmdbMovieDetailResponse getMovieDetail(Long movieId) {
        try {
            log.debug("Requesting movie detail for ID: {}", movieId);

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

        } catch (WebClientResponseException e) {
            log.error("Movie detail request failed for ID: {}, status: {}, body: {}",
                    movieId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("영화 상세 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * TV 시리즈 상세 정보 조회
     */
    public TmdbTvSeriesDetailResponse getTvSeriesDetail(Long tvId) {
        try {
            log.debug("Requesting TV series detail for ID: {}", tvId);

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

        } catch (WebClientResponseException e) {
            log.error("TV series detail request failed for ID: {}, status: {}, body: {}",
                    tvId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("TV 시리즈 상세 정보 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 멀티 검색 (영화 + TV 시리즈)
     */
    public List<TmdbMultiSearchResponse> searchMulti(String query, int page) {
        try {
            // page 값 검증
            int validatedPage = Math.max(1, Math.min(page, 1000));

            log.debug("Searching for: {} on page: {}", query, validatedPage);

            TmdbPagedResponse<TmdbMultiSearchResponse> response = tmdbWebClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/multi")
                            .queryParam(API_KEY_PARAM, apiKey)
                            .queryParam(LANGUAGE_PARAM, LANGUAGE)
                            .queryParam(PAGE_PARAM, validatedPage)
                            .queryParam(QUERY_PARAM, query)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<TmdbPagedResponse<TmdbMultiSearchResponse>>() {})
                    .block();

            return response != null ? response.getResults() : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("Multi search failed for query: {}, page: {}, status: {}, body: {}",
                    query, page, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("검색 요청 실패: " + e.getMessage(), e);
        }
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
        try {
            // page 값 검증
            int validatedPage = Math.max(1, Math.min(page, 1000));

            log.debug("Search request: {} for query: {} on page: {}", path, query, validatedPage);

            TmdbPagedResponse<T> response = tmdbWebClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam(API_KEY_PARAM, apiKey)
                            .queryParam(LANGUAGE_PARAM, LANGUAGE)
                            .queryParam(PAGE_PARAM, validatedPage)
                            .queryParam(QUERY_PARAM, query)
                            .build())
                    .retrieve()
                    .bodyToMono(typeRef)
                    .block();

            return response != null ? response.getResults() : Collections.emptyList();

        } catch (WebClientResponseException e) {
            log.error("Search request failed for path: {}, query: {}, page: {}, status: {}, body: {}",
                    path, query, page, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("검색 요청 실패: " + e.getMessage(), e);
        }
    }
}