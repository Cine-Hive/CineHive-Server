package com.example.CineHive.client;

import com.example.CineHive.dto.response.*;
import com.example.CineHive.exception.TmdbApiException; // 커스텀 예외 클래스
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${tmdb.api.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    // --- 상수 정의 ---
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String QUERY_PARAM = "query";
    private static final String SORT_BY_PARAM = "sort_by";
    private static final String WITH_GENRES_PARAM = "with_genres";
    private static final String RELEASE_DATE_GTE_PARAM = "release_date.gte";
    private static final String RELEASE_DATE_LTE_PARAM = "release_date.lte";
    private static final String FIRST_AIR_DATE_GTE_PARAM = "first_air_date.gte";
    private static final String FIRST_AIR_DATE_LTE_PARAM = "first_air_date.lte";
    private static final String APPEND_TO_RESPONSE_PARAM = "append_to_response";
    private static final String INCLUDE_IMAGE_LANGUAGE_PARAM = "include_image_language";

    private static final String DEFAULT_LANGUAGE = "ko-KR";
    private static final String ANIMATION_GENRE_ID = "16";

    private WebClient tmdbWebClient;

    // WebClient 인스턴스를 한 번만 생성하여 재사용
    private WebClient getTmdbWebClient() {
        if (this.tmdbWebClient == null) {
            this.tmdbWebClient = webClientBuilder.baseUrl(tmdbBaseUrl).build();
        }
        return this.tmdbWebClient;
    }

    // --- 공용 GET 요청 메서드 ---
    private <T> Mono<T> get(String path, ParameterizedTypeReference<T> responseType, MultiValueMap<String, String> queryParams) {
        // 모든 요청에 기본 파라미터 추가
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);

        log.debug("Requesting TMDB API: {}, params: {}", path, queryParams);

        return getTmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleApiError(response, path))
                .bodyToMono(responseType);
    }

    private <T> Mono<T> get(String path, Class<T> responseType, MultiValueMap<String, String> queryParams) {
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);

        log.debug("Requesting TMDB API: {}, params: {}", path, queryParams);

        return getTmdbWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> handleApiError(response, path))
                .bodyToMono(responseType);
    }

    // --- 영화 차트 API ---
    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getPopularMovies(int page) {
        return getPagedResponse("/movie/popular", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getTopRatedMovies(int page) {
        return getPagedResponse("/movie/top_rated", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getUpcomingMovies(int page) {
        return getPagedResponse("/movie/upcoming", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getNowPlayingMovies(int page) {
        return getPagedResponse("/movie/now_playing", page, new ParameterizedTypeReference<>() {});
    }

    // --- TV 시리즈 차트 API ---
    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getPopularTvSeries(int page) {
        return getPagedResponse("/tv/popular", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getTopRatedTvSeries(int page) {
        return getPagedResponse("/tv/top_rated", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getOnTheAirTvSeries(int page) {
        return getPagedResponse("/tv/on_the_air", page, new ParameterizedTypeReference<>() {});
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getAiringTodayTvSeries(int page) {
        return getPagedResponse("/tv/airing_today", page, new ParameterizedTypeReference<>() {});
    }

    // --- 상세 정보 API ---
    public Mono<TmdbMovieDetailResponse> getMovieDetail(Long movieId) {
        String path = "/movie/" + movieId;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE_PARAM, "credits,videos,images,recommendations,similar,keywords,watch/providers");
        params.add(INCLUDE_IMAGE_LANGUAGE_PARAM, "ko,null");
        return get(path, TmdbMovieDetailResponse.class, params);
    }

    public Mono<TmdbTvSeriesDetailResponse> getTvSeriesDetail(Long tvId) {
        String path = "/tv/" + tvId;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE_PARAM, "credits,videos,images,recommendations,similar,keywords,watch/providers");
        params.add(INCLUDE_IMAGE_LANGUAGE_PARAM, "ko,null");
        return get(path, TmdbTvSeriesDetailResponse.class, params);
    }

    // --- 검색 API ---
    public Mono<TmdbPagedResponse<TmdbMultiSearchResponse>> searchMulti(String query, int page) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(QUERY_PARAM, query);
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        return get("/search/multi", new ParameterizedTypeReference<>() {}, params);
    }

    // --- Discover (애니메이션 등) API ---
    public Mono<TmdbPagedResponse<TmdbMovieResponse>> discoverAnimationMovies(int page, String sortBy) {
        return discoverMovies(page, sortBy, ANIMATION_GENRE_ID, null, null);
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> discoverNowPlayingAnimationMovies(int page) {
        String today = LocalDate.now().toString();
        // 현재 상영중인 작품은 인기도 순으로 정렬하는 것이 일반적
        return discoverMovies(page, "popularity.desc", ANIMATION_GENRE_ID, null, today);
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> discoverUpcomingAnimationMovies(int page) {
        String today = LocalDate.now().toString();
        // 개봉 예정작은 개봉일 순으로 정렬
        return discoverMovies(page, "release_date.asc", ANIMATION_GENRE_ID, today, null);
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> discoverAnimationTvSeries(int page, String sortBy) {
        return discoverTvSeries(page, sortBy, ANIMATION_GENRE_ID, null, null);
    }

    // --- Private Helper Methods ---

    /**
     * 페이지네이션이 있는 API를 호출하는 공통 메서드
     */
    private <T> Mono<TmdbPagedResponse<T>> getPagedResponse(String path, int page, ParameterizedTypeReference<TmdbPagedResponse<T>> typeRef) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        return get(path, typeRef, params);
    }

    /**
     * 영화 Discover API를 호출하는 공통 메서드
     */
    private Mono<TmdbPagedResponse<TmdbMovieResponse>> discoverMovies(int page, String sortBy, String genreIds, String releaseDateGte, String releaseDateLte) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        addQueryParamIfPresent(params, SORT_BY_PARAM, sortBy);
        addQueryParamIfPresent(params, WITH_GENRES_PARAM, genreIds);
        addQueryParamIfPresent(params, RELEASE_DATE_GTE_PARAM, releaseDateGte);
        addQueryParamIfPresent(params, RELEASE_DATE_LTE_PARAM, releaseDateLte);

        return get("/discover/movie", new ParameterizedTypeReference<>() {}, params);
    }

    /**
     * TV 시리즈 Discover API를 호출하는 공통 메서드
     */
    private Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> discoverTvSeries(int page, String sortBy, String genreIds, String firstAirDateGte, String firstAirDateLte) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        addQueryParamIfPresent(params, SORT_BY_PARAM, sortBy);
        addQueryParamIfPresent(params, WITH_GENRES_PARAM, genreIds);
        addQueryParamIfPresent(params, FIRST_AIR_DATE_GTE_PARAM, firstAirDateGte);
        addQueryParamIfPresent(params, FIRST_AIR_DATE_LTE_PARAM, firstAirDateLte);

        return get("/discover/tv", new ParameterizedTypeReference<>() {}, params);
    }

    /**
     * WebClient 에러를 처리하는 공통 핸들러
     */
    private Mono<Throwable> handleApiError(ClientResponse response, String path) {
        return response.bodyToMono(String.class)
                .switchIfEmpty(Mono.just("Response body is empty")) // 응답 본문이 비었을 때 기본 메시지 제공
                .flatMap(errorBody -> {
                    log.error("TMDB API Error for path [{}], Status: {}, Body: {}",
                            path, response.statusCode(), errorBody);
                    // 항상 TmdbApiException을 포함하는 Mono<Throwable>을 반환합니다.
                    return Mono.error(new TmdbApiException("TMDB API 요청 실패: " + response.statusCode()));
                });
    }

    /**
     * TMDB API의 페이지 파라미터 유효성 검사 (1 ~ 1000)
     */
    private int validatePage(int page) {
        return Math.max(1, Math.min(page, 1000));
    }

    /**
     * 파라미터 값이 null이 아닐 경우에만 추가하는 유틸리티 메서드
     */
    private void addQueryParamIfPresent(MultiValueMap<String, String> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.add(key, value);
        }
    }
}