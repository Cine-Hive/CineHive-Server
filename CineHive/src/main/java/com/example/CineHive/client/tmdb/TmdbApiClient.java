package com.example.CineHive.client.tmdb;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.domain.media.dto.ChartProperties;
import com.example.CineHive.dto.tmdb.*;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * TMDB (The Movie Database) API와의 통신을 담당하는 클라이언트입니다.
 * WebClient를 사용하여 비동기 방식으로 API를 호출합니다.
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
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String QUERY_PARAM = "query";
    private static final String SORT_BY_PARAM = "sort_by";
    private static final String WITH_GENRES_PARAM = "with_genres";
    private static final String WITH_COMPANIES_PARAM = "with_companies";
    private static final String WITH_NETWORKS_PARAM = "with_networks";
    private static final String WITH_ORIGIN_COUNTRY_PARAM = "with_origin_country";
    private static final String WITH_ORIGINAL_LANGUAGE_PARAM = "with_original_language";
    private static final String WITH_KEYWORDS_PARAM = "with_keywords";
    private static final String APPEND_TO_RESPONSE_PARAM = "append_to_response";
    private static final String INCLUDE_IMAGE_LANGUAGE_PARAM = "include_image_language";
    private static final String VOTE_COUNT_GTE_PARAM = "vote_count.gte";
    private static final String RELEASE_DATE_GTE_PARAM = "primary_release_date.gte";
    private static final String RELEASE_DATE_LTE_PARAM = "primary_release_date.lte";
    private static final String FIRST_AIR_DATE_GTE_PARAM = "first_air_date.gte";
    private static final String FIRST_AIR_DATE_LTE_PARAM = "first_air_date.lte";
    private static final String WITH_CAST_PARAM = "with_cast";
    private static final String WITH_NUMBER_OF_SEASONS_PARAM = "with_number_of_seasons";


    private static final String DEFAULT_LANGUAGE = "ko-KR";
    private static final String MIN_VOTE_COUNT_FOR_RATING_SORT = "500";

    @PostConstruct
    public void init() {
        this.tmdbWebClient = webClientBuilder.baseUrl(tmdbBaseUrl).build();
    }

    // --- 기본 영화 차트 API ---
    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getPopularMovies(int page) {
        String path = "/movie/popular";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getTopRatedMovies(int page) {
        String path = "/movie/top_rated";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getUpcomingMovies(int page) {
        String path = "/movie/upcoming";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getNowPlayingMovies(int page) {
        String path = "/movie/now_playing";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    // --- 기본 TV 시리즈 차트 API ---
    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getPopularTvSeries(int page) {
        String path = "/tv/popular";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getTopRatedTvSeries(int page) {
        String path = "/tv/top_rated";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getOnTheAirTvSeries(int page) {
        String path = "/tv/on_the_air";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getAiringTodayTvSeries(int page) {
        String path = "/tv/airing_today";
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    // --- 트렌드 API ---
    public Mono<TmdbPagedResponse<TmdbMovieResponse>> getTrendingMovies(String timeWindow, int page) {
        String path = "/trending/movie/" + timeWindow;
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> getTrendingTv(String timeWindow, int page) {
        String path = "/trending/tv/" + timeWindow;
        MultiValueMap<String, String> params = createPageParams(page);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    // --- 상세 정보 API (제네릭이 아니므로 기존 방식 유지) ---
    public Mono<TmdbMovieDetailResponse> getMovieDetail(Long movieId) {
        MultiValueMap<String, String> params = createDetailParams();
        return get("/movie/" + movieId, TmdbMovieDetailResponse.class, params);
    }

    public Mono<TmdbTvSeriesDetailResponse> getTvSeriesDetail(Long tvId) {
        MultiValueMap<String, String> params = createDetailParams();
        return get("/tv/" + tvId, TmdbTvSeriesDetailResponse.class, params);
    }

    // --- 검색 API ---
    public Mono<TmdbPagedResponse<TmdbMultiSearchResponse>> searchMulti(String query, int page) {
        String path = "/search/multi";
        MultiValueMap<String, String> params = createPageParams(page);
        params.add(QUERY_PARAM, query);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    // --- Discover API ---
    public Mono<TmdbPagedResponse<TmdbMovieResponse>> discoverMovies(int page, ChartProperties props) {
        String path = "/discover/movie";
        MultiValueMap<String, String> params = buildDiscoverParams(page, props);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    public Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> discoverTvSeries(int page, ChartProperties props) {
        String path = "/discover/tv";
        MultiValueMap<String, String> params = buildDiscoverParams(page, props);
        return getRaw(path, params).map(json -> parseResponse(json, new TypeReference<>() {}));
    }

    // --- 메타데이터 API (제네릭이 아니므로 기존 방식 유지) ---
    public Mono<TmdbGenresResponse> getMovieGenres() {
        return get("/genre/movie/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>());
    }

    public Mono<TmdbGenresResponse> getTvGenres() {
        return get("/genre/tv/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>());
    }

    public Mono<TmdbNetworkImagesResponse> getNetworkImages(Long networkId) {
        return get("/network/" + networkId + "/images", TmdbNetworkImagesResponse.class, new LinkedMultiValueMap<>());
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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        addQueryParamIfPresent(params, SORT_BY_PARAM, props.sortBy());
        addQueryParamIfPresent(params, WITH_GENRES_PARAM, props.genreId());
        addQueryParamIfPresent(params, WITH_COMPANIES_PARAM, props.companyId());
        addQueryParamIfPresent(params, WITH_ORIGIN_COUNTRY_PARAM, props.originCountry());
        addQueryParamIfPresent(params, WITH_KEYWORDS_PARAM, props.keywordId());
        addQueryParamIfPresent(params, WITH_NETWORKS_PARAM, props.networkId());
        addQueryParamIfPresent(params, WITH_ORIGINAL_LANGUAGE_PARAM, props.withOriginalLanguage());
        addQueryParamIfPresent(params, RELEASE_DATE_GTE_PARAM, props.releaseDateGte());
        addQueryParamIfPresent(params, RELEASE_DATE_LTE_PARAM, props.releaseDateLte());
        addQueryParamIfPresent(params, FIRST_AIR_DATE_GTE_PARAM, props.firstAirDateGte());
        addQueryParamIfPresent(params, FIRST_AIR_DATE_LTE_PARAM, props.firstAirDateLte());
        addQueryParamIfPresent(params, WITH_CAST_PARAM, props.withCast());
        addQueryParamIfPresent(params, WITH_NUMBER_OF_SEASONS_PARAM, props.numberOfSeasons());

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