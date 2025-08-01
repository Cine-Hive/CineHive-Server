package com.example.CineHive.client.tmdb;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.domain.media.dto.ChartProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * TMDB (The Movie Database) API와의 통신을 담당하는 클라이언트입니다.
 * WebClient를 사용하여 API를 동기 방식으로 호출합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    private WebClient tmdbWebClient;

    // --- 상수 정의 ---
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String QUERY_PARAM = "query";
    private static final String SORT_BY_PARAM = "sort_by";
    private static final String WITH_GENRES_PARAM = "with_genres";
    private static final String WITH_NETWORKS_PARAM = "with_networks";
    private static final String VOTE_COUNT_GTE_PARAM = "vote_count.gte";
    private static final String DEFAULT_LANGUAGE = "ko-KR";
    private static final String MIN_VOTE_COUNT_FOR_RATING_SORT = "500";

    @PostConstruct
    public void init() {
        this.tmdbWebClient = webClientBuilder.baseUrl(tmdbBaseUrl).build();
    }

    // --- 기본 영화 차트 API ---
    public TmdbPagedResponse<TmdbMovieResponse> getPopularMovies(int page) {
        String json = getRaw("/movie/popular", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getTopRatedMovies(int page) {
        String json = getRaw("/movie/top_rated", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getUpcomingMovies(int page) {
        String json = getRaw("/movie/upcoming", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getNowPlayingMovies(int page) {
        String json = getRaw("/movie/now_playing", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 기본 TV 시리즈 차트 API ---
    public TmdbPagedResponse<TmdbTvSeriesResponse> getPopularTvSeries(int page) {
        String json = getRaw("/tv/popular", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getTopRatedTvSeries(int page) {
        String json = getRaw("/tv/top_rated", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getOnTheAirTvSeries(int page) {
        String json = getRaw("/tv/on_the_air", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getAiringTodayTvSeries(int page) {
        String json = getRaw("/tv/airing_today", createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 트렌드 API ---
    public TmdbPagedResponse<TmdbMovieResponse> getTrendingMovies(String timeWindow, int page) {
        String path = "/trending/movie/" + timeWindow;
        String json = getRaw(path, createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getTrendingTv(String timeWindow, int page) {
        String path = "/trending/tv/" + timeWindow;
        String json = getRaw(path, createPageParams(page)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 상세 정보 API ---
    public TmdbMovieDetailResponse getMovieDetail(Long movieId) {
        return get("/movie/" + movieId, TmdbMovieDetailResponse.class, createDetailParams()).block(TIMEOUT);
    }

    public TmdbTvSeriesDetailResponse getTvSeriesDetail(Long tvId) {
        return get("/tv/" + tvId, TmdbTvSeriesDetailResponse.class, createDetailParams()).block(TIMEOUT);
    }

    // --- 검색 API ---
    public TmdbPagedResponse<TmdbMultiSearchResponse> searchMulti(String query, int page) {
        MultiValueMap<String, String> params = createPageParams(page);
        params.add(QUERY_PARAM, query);
        String json = getRaw("/search/multi", params).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- Discover API ---
    public TmdbPagedResponse<TmdbMovieResponse> discoverMovies(int page, ChartProperties props) {
        String json = getRaw("/discover/movie", buildDiscoverParams(page, props)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> discoverTvSeries(int page, ChartProperties props) {
        String json = getRaw("/discover/tv", buildDiscoverParams(page, props)).block(TIMEOUT);
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 메타데이터 API ---
    public TmdbGenresResponse getMovieGenres() {
        return get("/genre/movie/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>()).block(TIMEOUT);
    }

    public TmdbGenresResponse getTvGenres() {
        return get("/genre/tv/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>()).block(TIMEOUT);
    }

    public TmdbNetworkImagesResponse getNetworkImages(Long networkId) {
        return get("/network/" + networkId + "/images", TmdbNetworkImagesResponse.class, new LinkedMultiValueMap<>()).block(TIMEOUT);
    }

    // --- Private Helper Methods ---

    private <T> T parseResponse(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 역직렬화에 실패했습니다.", e);
            throw new BusinessException("API 응답 처리 중 오류가 발생했습니다.", ErrorCode.MAPPING_ERROR);
        }
    }

    private <T> Mono<T> get(String path, Class<T> responseType, MultiValueMap<String, String> queryParams) {
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleApiError(response, path))
                .bodyToMono(responseType);
    }

    private Mono<String> getRaw(String path, MultiValueMap<String, String> queryParams) {
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleApiError(response, path))
                .bodyToMono(String.class);
    }

    private MultiValueMap<String, String> createPageParams(int page) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        return params;
    }

    private MultiValueMap<String, String> createDetailParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("append_to_response", "credits,videos,images,recommendations,similar,keywords,watch/providers");
        params.add("include_image_language", "ko,null");
        return params;
    }

    private MultiValueMap<String, String> buildDiscoverParams(int page, ChartProperties props) {
        MultiValueMap<String, String> params = createPageParams(page);
        addQueryParamIfPresent(params, SORT_BY_PARAM, props.sortBy());
        addQueryParamIfPresent(params, WITH_GENRES_PARAM, props.genreId());
        addQueryParamIfPresent(params, WITH_NETWORKS_PARAM, props.networkId());

        if (props.sortBy() != null && props.sortBy().contains("vote_average")) {
            params.add(VOTE_COUNT_GTE_PARAM, MIN_VOTE_COUNT_FOR_RATING_SORT);
        }
        return params;
    }

    private void addQueryParamIfPresent(MultiValueMap<String, String> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.add(key, value);
        }
    }

    private int validatePage(int page) {
        return Math.max(1, Math.min(page, 500));
    }

    private Mono<Throwable> handleApiError(ClientResponse response, String path) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("응답 본문 없음")
                .flatMap(errorBody -> {
                    log.error("TMDB API 오류 발생. 경로: [{}], 상태 코드: {}, 응답 본문: {}",
                            path, response.statusCode(), errorBody);
                    return Mono.error(new BusinessException("TMDB API 요청 실패", ErrorCode.TMDB_API_ERROR));
                });
    }
}