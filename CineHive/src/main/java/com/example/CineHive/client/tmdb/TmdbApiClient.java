package com.example.CineHive.client.tmdb;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.domain.media.dto.ChartProperties;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.exception.TmdbClientException;
import com.example.CineHive.global.properties.TmdbProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * TMDB (The Movie Database) API와의 통신을 담당하는 클라이언트입니다.
 * RestClient를 사용하여 완벽한 동기(Synchronous) 방식으로 API를 호출합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;

    private RestClient tmdbRestClient;
    private String apiKey;

    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String QUERY_PARAM = "query";
    private static final String SORT_BY_PARAM = "sort_by";
    private static final String WITH_GENRES_PARAM = "with_genres";
    private static final String WITH_NETWORKS_PARAM = "with_networks";
    private static final String VOTE_COUNT_GTE_PARAM = "vote_count.gte";
    private static final String APPEND_TO_RESPONSE = "append_to_response";
    private static final String INCLUDE_IMAGE_LANGUAGE = "include_image_language";
    private static final String DEFAULT_LANGUAGE = "ko-KR";
    private static final String MIN_VOTE_COUNT_FOR_RATING_SORT = "500";

    /**
     * 의존성 주입이 완료된 후, RestClient와 API 키를 초기화합니다.
     */
    @PostConstruct
    public void init() {
        this.tmdbRestClient = restClientBuilder.baseUrl(tmdbProperties.getBaseUrl()).build();
        this.apiKey = tmdbProperties.getApiKey();
    }

    // --- 기본 영화 차트 API ---

    /**
     * 인기 있는 영화 목록을 조회합니다. (popular)
     * @param page 조회할 페이지 번호 (1부터 시작)
     * @return 페이징된 영화 목록 응답
     */
    public TmdbPagedResponse<TmdbMovieResponse> getPopularMovies(int page) {
        String json = getRaw("/movie/popular", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getTopRatedMovies(int page) {
        String json = getRaw("/movie/top_rated", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getUpcomingMovies(int page) {
        String json = getRaw("/movie/upcoming", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbMovieResponse> getNowPlayingMovies(int page) {
        String json = getRaw("/movie/now_playing", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 기본 TV 시리즈 차트 API ---
    public TmdbPagedResponse<TmdbTvSeriesResponse> getPopularTvSeries(int page) {
        String json = getRaw("/tv/popular", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getTopRatedTvSeries(int page) {
        String json = getRaw("/tv/top_rated", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getOnTheAirTvSeries(int page) {
        String json = getRaw("/tv/on_the_air", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getAiringTodayTvSeries(int page) {
        String json = getRaw("/tv/airing_today", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 트렌드 API ---
    public TmdbPagedResponse<TmdbMovieResponse> getTrendingMovies(String timeWindow, int page) {
        String path = "/trending/movie/" + timeWindow;
        String json = getRaw(path, createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> getTrendingTv(String timeWindow, int page) {
        String path = "/trending/tv/" + timeWindow;
        String json = getRaw(path, createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 인물 관련 API ---

    /**
     * 인기 있는 인물 목록을 조회합니다. (popular)
     * @param page 조회할 페이지 번호 (1부터 시작)
     * @return 페이징된 인물 목록 응답
     */
    public TmdbPagedResponse<TmdbPersonInListResponse> getPopularPeople(int page) {
        String json = getRaw("/person/popular", createPageParams(page));
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 상세 정보 API ---

    /**
     * 특정 영화의 상세 정보를 조회합니다.
     * @param movieId 조회할 영화의 TMDB 고유 ID
     * @return 영화 상세 정보 응답 DTO
     */
    public TmdbMovieDetailResponse getMovieDetail(Long movieId) {
        return get("/movie/" + movieId, TmdbMovieDetailResponse.class, createMediaDetailParams());
    }

    /**
     * 특정 TV 시리즈의 상세 정보를 조회합니다.
     * @param tvId 조회할 TV 시리즈의 TMDB 고유 ID
     * @return TV 시리즈 상세 정보 응답 DTO
     */
    public TmdbTvSeriesDetailResponse getTvSeriesDetail(Long tvId) {
        return get("/tv/" + tvId, TmdbTvSeriesDetailResponse.class, createMediaDetailParams());
    }

    /**
     * 특정 인물의 상세 정보를 조회합니다.
     * @param personId 조회할 인물의 TMDB 고유 ID
     * @return 인물 상세 정보 응답 DTO
     */
    public TmdbPersonDetailResponse getPersonDetail(Long personId) {
        return get("/person/" + personId, TmdbPersonDetailResponse.class, createPersonDetailParams());
    }

    // --- 검색 API ---

    /**
     * 영화, TV, 인물 등 여러 미디어 타입을 통합하여 검색합니다.
     * @param query 검색어
     * @param page 조회할 페이지 번호 (1부터 시작)
     * @return 페이징된 통합 검색 결과 응답
     */
    public TmdbPagedResponse<TmdbMultiSearchResponse> searchMulti(String query, int page) {
        MultiValueMap<String, String> params = createPageParams(page);
        params.add(QUERY_PARAM, query);
        String json = getRaw("/search/multi", params);
        return parseResponse(json, new TypeReference<>() {});
    }

    /**
     * 특정 기간 동안 변경된 영화 ID 목록을 조회합니다.
     * @param startDate 조회 시작일 (YYYY-MM-DD 형식)
     * @param page 페이지 번호
     * @return 변경된 영화 목록 응답
     */
    public TmdbChangesResponse getMovieChanges(String startDate, int page) {
        MultiValueMap<String, String> params = createPageParams(page);
        params.add("start_date", startDate);
        return get("/movie/changes", TmdbChangesResponse.class, params);
    }

    /**
     * 특정 기간 동안 변경된 TV 시리즈 ID 목록을 조회합니다.
     * @param startDate 조회 시작일 (YYYY-MM-DD 형식)
     * @param page 페이지 번호
     * @return 변경된 TV 시리즈 목록 응답
     */
    public TmdbChangesResponse getTvChanges(String startDate, int page) {
        MultiValueMap<String, String> params = createPageParams(page);
        params.add("start_date", startDate);
        return get("/tv/changes", TmdbChangesResponse.class, params);
    }

    // --- Discover API ---
    public TmdbPagedResponse<TmdbMovieResponse> discoverMovies(int page, ChartProperties props) {
        String json = getRaw("/discover/movie", buildDiscoverParams(page, props));
        return parseResponse(json, new TypeReference<>() {});
    }

    public TmdbPagedResponse<TmdbTvSeriesResponse> discoverTvSeries(int page, ChartProperties props) {
        String json = getRaw("/discover/tv", buildDiscoverParams(page, props));
        return parseResponse(json, new TypeReference<>() {});
    }

    // --- 메타데이터 API ---
    public TmdbGenresResponse getMovieGenres() {
        return get("/genre/movie/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>());
    }

    public TmdbGenresResponse getTvGenres() {
        return get("/genre/tv/list", TmdbGenresResponse.class, new LinkedMultiValueMap<>());
    }

    public TmdbNetworkImagesResponse getNetworkImages(Long networkId) {
        return get("/network/" + networkId + "/images", TmdbNetworkImagesResponse.class, new LinkedMultiValueMap<>());
    }

    /**
     * TMDB의 API Configuration 정보를 조회합니다.
     * 이미지 base URL, 사이즈 목록 등을 포함합니다.
     * @return Configuration 정보 응답 DTO
     */
    public TmdbConfigurationResponse getConfiguration() {
        return get("/configuration", TmdbConfigurationResponse.class, new LinkedMultiValueMap<>());
    }

    // --- Batch Sync API ---

    /**
     * TMDB Daily Export 파일을 다운로드합니다.
     * @param fileDate 파일 날짜 (MM_dd_yyyy 형식)
     * @param entityType 엔티티 타입 (movie, tv, person)
     * @return 압축된 NDJSON 파일의 바이트 배열
     */
    public byte[] downloadDailyExport(String fileDate, String entityType) {
        String baseUrl = tmdbProperties.getExportBaseUrl();
        String fileName = entityType + "_ids_" + fileDate + ".json.gz";
        String url = baseUrl + "/" + fileName;
        
        try {
            return RestClient.create()
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new TmdbClientException(
                        "Daily export download failed for " + fileName,
                        (HttpStatus) response.getStatusCode()
                    );
                })
                .body(byte[].class);
        } catch (Exception e) {
            log.error("TMDB Daily Export 다운로드 실패. 파일명: {}", fileName, e);
            throw new TmdbClientException("Daily export download failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 영화 상세 정보를 배치 처리용으로 조회합니다.
     * append_to_response로 한 번에 최대 정보를 가져옵니다.
     * @param movieId 영화 TMDB ID
     * @return 영화 상세 정보 (credits, keywords, images 포함)
     */
    public TmdbMovieDetailResponse getMovieDetailForBatch(Long movieId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "credits,keywords,images");
        params.add(INCLUDE_IMAGE_LANGUAGE, "ko,null");
        
        try {
            return get("/movie/" + movieId, TmdbMovieDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("영화 ID {}를 찾을 수 없습니다.", movieId);
                throw e;
            }
            throw e;
        }
    }
    
    /**
     * TV 시리즈 상세 정보를 배치 처리용으로 조회합니다.
     * append_to_response로 한 번에 최대 정보를 가져옵니다.
     * @param tvId TV 시리즈 TMDB ID
     * @return TV 시리즈 상세 정보 (aggregate_credits, keywords, images 포함)
     */
    public TmdbTvSeriesDetailResponse getTvDetailForBatch(Long tvId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "aggregate_credits,keywords,images");
        params.add(INCLUDE_IMAGE_LANGUAGE, "ko,null");
        
        try {
            return get("/tv/" + tvId, TmdbTvSeriesDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("TV 시리즈 ID {}를 찾을 수 없습니다.", tvId);
                throw e;
            }
            throw e;
        }
    }
    
    /**
     * 인물 상세 정보를 배치 처리용으로 조회합니다.
     * append_to_response로 한 번에 최대 정보를 가져옵니다.
     * @param personId 인물 TMDB ID
     * @return 인물 상세 정보 (movie_credits, tv_credits, images 포함)
     */
    public TmdbPersonDetailResponse getPersonDetailForBatch(Long personId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "movie_credits,tv_credits,images");
        
        try {
            return get("/person/" + personId, TmdbPersonDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("인물 ID {}를 찾을 수 없습니다.", personId);
                throw e;
            }
            throw e;
        }
    }
    
    /**
     * 컬렉션 상세 정보를 조회합니다.
     * @param collectionId 컬렉션 TMDB ID
     * @return 컬렉션 상세 정보
     */
    public TmdbCollectionDetailResponse getCollectionDetail(Long collectionId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "images");
        
        try {
            return get("/collection/" + collectionId, TmdbCollectionDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("컬렉션 ID {}를 찾을 수 없습니다.", collectionId);
                throw e;
            }
            throw e;
        }
    }
    
    /**
     * TV 시즌 상세 정보를 조회합니다.
     * @param tvId TV 시리즈 TMDB ID
     * @param seasonNumber 시즌 번호
     * @return 시즌 상세 정보
     */
    public TmdbSeasonDetailResponse getSeasonDetail(Long tvId, Integer seasonNumber) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "credits,images,videos,external_ids");
        
        try {
            return get("/tv/" + tvId + "/season/" + seasonNumber, TmdbSeasonDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("TV {} 시즌 {}를 찾을 수 없습니다.", tvId, seasonNumber);
                throw e;
            }
            throw e;
        }
    }
    
    /**
     * 에피소드 상세 정보를 조회합니다.
     * @param tvId TV 시리즈 TMDB ID
     * @param seasonNumber 시즌 번호
     * @param episodeNumber 에피소드 번호
     * @return 에피소드 상세 정보
     */
    public TmdbEpisodeDetailResponse getEpisodeDetail(Long tvId, Integer seasonNumber, Integer episodeNumber) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "credits,images,videos");
        
        try {
            return get("/tv/" + tvId + "/season/" + seasonNumber + "/episode/" + episodeNumber, 
                      TmdbEpisodeDetailResponse.class, params);
        } catch (TmdbClientException e) {
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("TV {} 시즌 {} 에피소드 {}를 찾을 수 없습니다.", tvId, seasonNumber, episodeNumber);
                throw e;
            }
            throw e;
        }
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

    private <T> T get(String path, Class<T> responseType, MultiValueMap<String, String> queryParams) {
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);
        return tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> handleApiError(response, path))
                .body(responseType);
    }

    private String getRaw(String path, MultiValueMap<String, String> queryParams) {
        queryParams.add(API_KEY_PARAM, apiKey);
        queryParams.add(LANGUAGE_PARAM, DEFAULT_LANGUAGE);
        return tmdbRestClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParams(queryParams).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> handleApiError(response, path))
                .body(String.class);
    }

    private MultiValueMap<String, String> createPageParams(int page) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(PAGE_PARAM, String.valueOf(validatePage(page)));
        return params;
    }

    private MultiValueMap<String, String> createMediaDetailParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "credits,videos,images,recommendations,similar,keywords,watch/providers");
        params.add(INCLUDE_IMAGE_LANGUAGE, "ko,null");
        return params;
    }

    private MultiValueMap<String, String> createPersonDetailParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(APPEND_TO_RESPONSE, "movie_credits,tv_credits,images");
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

    private void handleApiError(ClientHttpResponse response, String path) throws IOException {
        String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        if (errorBody.isEmpty()) {
            errorBody = "응답 본문 없음";
        }

        log.error("TMDB API 오류 발생. 경로: [{}], 상태 코드: {}, 응답 본문: {}",
                path, response.getStatusCode(), errorBody);

        throw new TmdbClientException(errorBody, (HttpStatus) response.getStatusCode());
    }
}