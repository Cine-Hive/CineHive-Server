package com.example.CineHive.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TMDB API URL을 빌더 패턴을 사용하여 생성하는 유틸리티 클래스
 */
@Component
public class TmdbUrlBuilder {
    // 상수 정의
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String DEFAULT_LANGUAGE = "ko-KR";
    private static final int ANIMATION_GENRE_ID = 16;

    // API 엔드포인트 경로 상수
    private static final String MOVIE_PATH = "/movie";
    private static final String TV_PATH = "/tv";
    private static final String DISCOVER_PATH = "/discover";

    // 파라미터 키 상수
    private static final String API_KEY_PARAM = "api_key";
    private static final String LANGUAGE_PARAM = "language";
    private static final String PAGE_PARAM = "page";
    private static final String APPEND_TO_RESPONSE_PARAM = "append_to_response";
    private static final String WITH_GENRES_PARAM = "with_genres";
    private static final String SORT_BY_PARAM = "sort_by";

    // API 응답 필드 상수
    private static final String VIDEOS_FIELD = "videos";
    private static final String CREDITS_FIELD = "credits";
    private static final String IMAGES_FIELD = "images";
    private static final String RECOMMENDATIONS_FIELD = "recommendations";
    private static final String SIMILAR_FIELD = "similar";
    private static final String KEYWORDS_FIELD = "keywords";

    @Value("${tmdb.api.key}")
    private String apiKey;

    private final StringBuilder pathBuilder;
    private final Map<String, String> queryParams;

    /**
     * 새 URL 빌더 인스턴스 생성
     */
    public TmdbUrlBuilder() {
        this.pathBuilder = new StringBuilder();
        this.queryParams = new HashMap<>();
    }

    /**
     * 새 빌드 세션 초기화
     * @return 초기화된 빌더 인스턴스
     */
    public TmdbUrlBuilder init() {
        this.pathBuilder.setLength(0);
        this.queryParams.clear();
        this.queryParams.put(API_KEY_PARAM, apiKey);
        return this;
    }

    /**
     * API 경로 추가
     * @param path 추가할 API 경로
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder path(String path) {
        if (path == null || path.isEmpty()) {
            return this;
        }

        if (!path.startsWith("/")) {
            pathBuilder.append("/");
        }
        pathBuilder.append(path);
        return this;
    }

    /**
     * 쿼리 파라미터 추가 (문자열)
     * @param key 파라미터 키
     * @param value 파라미터 값
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder param(String key, String value) {
        if (key != null && !key.isEmpty() && value != null) {
            queryParams.put(key, value);
        }
        return this;
    }

    /**
     * 쿼리 파라미터 추가 (정수)
     * @param key 파라미터 키
     * @param value 파라미터 값
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder param(String key, int value) {
        return param(key, String.valueOf(value));
    }

    /**
     * 쿼리 파라미터 추가 (Boolean)
     * @param key 파라미터 키
     * @param value 파라미터 값
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder param(String key, boolean value) {
        return param(key, String.valueOf(value));
    }

    /**
     * 언어 파라미터 추가 (기본값: ko-KR)
     * @param language 언어 코드 (null인 경우 기본값 사용)
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder language(String language) {
        return param(LANGUAGE_PARAM, language != null ? language : DEFAULT_LANGUAGE);
    }

    /**
     * 페이지 파라미터 추가
     * @param page 페이지 번호
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder page(int page) {
        if (page < 1) {
            page = 1; // 유효하지 않은 페이지 번호 처리
        }
        return param(PAGE_PARAM, page);
    }

    /**
     * append_to_response 파라미터 추가 (여러 정보 한 번에 요청)
     * @param fields 요청할 추가 필드 목록
     * @return 빌더 인스턴스
     */
    public TmdbUrlBuilder appendToResponse(String... fields) {
        if (fields != null && fields.length > 0) {
            String fieldsStr = Arrays.stream(fields)
                    .filter(field -> field != null && !field.isEmpty())
                    .collect(Collectors.joining(","));

            if (!fieldsStr.isEmpty()) {
                return param(APPEND_TO_RESPONSE_PARAM, fieldsStr);
            }
        }
        return this;
    }

    /**
     * 최종 URL 문자열 생성
     * @return 완성된 URL 문자열
     */
    public String build() {
        // API 키 확인
        if (!queryParams.containsKey(API_KEY_PARAM)) {
            queryParams.put(API_KEY_PARAM, apiKey);
        }

        StringBuilder url = new StringBuilder(BASE_URL).append(pathBuilder);

        if (!queryParams.isEmpty()) {
            url.append("?");
            queryParams.forEach((k, v) -> {
                url.append(encode(k)).append("=").append(encode(v)).append("&");
            });

            // 마지막 & 제거
            if (url.charAt(url.length() - 1) == '&') {
                url.deleteCharAt(url.length() - 1);
            }
        }

        return url.toString();
    }

    /**
     * URL 안전 인코딩
     * @param value 인코딩할 문자열
     * @return 인코딩된 문자열
     */
    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    //
    // 미디어 목록 관련 메소드
    //

    /**
     * 영화 목록 URL 생성
     * @param category 영화 카테고리 (popular, top_rated, now_playing, upcoming)
     * @param page 페이지 번호
     * @return 완성된 URL 문자열
     */
    public String movieList(String category, int page) {
        return init()
                .path(MOVIE_PATH + "/" + category)
                .language(DEFAULT_LANGUAGE)
                .page(page)
                .build();
    }

    /**
     * TV 시리즈 목록 URL 생성
     * @param category TV 카테고리 (popular, top_rated, on_the_air, airing_today)
     * @param page 페이지 번호
     * @return 완성된 URL 문자열
     */
    public String tvList(String category, int page) {
        return init()
                .path(TV_PATH + "/" + category)
                .language(DEFAULT_LANGUAGE)
                .page(page)
                .build();
    }

    /**
     * 애니메이션 목록 URL 생성 (장르 ID 16 사용)
     * @param sortBy 정렬 기준 (popularity.desc, vote_average.desc 등)
     * @param page 페이지 번호
     * @return 완성된 URL 문자열
     */
    public String animationList(String sortBy, int page) {
        return init()
                .path(DISCOVER_PATH + MOVIE_PATH)
                .param(WITH_GENRES_PARAM, String.valueOf(ANIMATION_GENRE_ID))
                .param(SORT_BY_PARAM, sortBy)
                .language(DEFAULT_LANGUAGE)
                .page(page)
                .build();
    }

    //
    // 미디어 상세 정보 관련 메소드
    //
    /**
     * 미디어 전체 상세 정보 URL 생성 (출연진, 비디오, 이미지, 추천작 등 포함)
     * @param mediaType 미디어 타입 (movie, tv)
     * @param id 미디어 ID
     * @return 완성된 URL 문자열
     */
    public String mediaDetail(String mediaType, long id) {
        return init()
                .path("/" + mediaType + "/" + id)
                .language(DEFAULT_LANGUAGE)
                .appendToResponse(
                        VIDEOS_FIELD,
                        CREDITS_FIELD,
                        IMAGES_FIELD,
                        RECOMMENDATIONS_FIELD,
                        SIMILAR_FIELD,
                        KEYWORDS_FIELD
                )
                .build();
    }
}