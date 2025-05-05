package com.example.CineHive.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * TMDB API URL을 빌더 패턴을 사용하여 생성하는 유틸리티 클래스
 */
@Component
public class TmdbUrlBuilder {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    
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
     */
    public TmdbUrlBuilder init() {
        this.pathBuilder.setLength(0);
        this.queryParams.clear();
        this.queryParams.put("api_key", apiKey);
        return this;
    }

    /**
     * API 경로 추가
     */
    public TmdbUrlBuilder path(String path) {
        if (!path.startsWith("/")) {
            pathBuilder.append("/");
        }
        pathBuilder.append(path);
        return this;
    }

    /**
     * 쿼리 파라미터 추가 (문자열)
     */
    public TmdbUrlBuilder param(String key, String value) {
        if (value != null) {
            queryParams.put(key, value);
        }
        return this;
    }

    /**
     * 쿼리 파라미터 추가 (정수)
     */
    public TmdbUrlBuilder param(String key, int value) {
        return param(key, String.valueOf(value));
    }
    
    /**
     * 쿼리 파라미터 추가 (Boolean)
     */
    public TmdbUrlBuilder param(String key, boolean value) {
        return param(key, String.valueOf(value));
    }
    
    /**
     * 언어 파라미터 추가 (기본값: ko-KR)
     */
    public TmdbUrlBuilder language(String language) {
        return param("language", language != null ? language : "ko-KR");
    }
    
    /**
     * 페이지 파라미터 추가
     */
    public TmdbUrlBuilder page(int page) {
        return param("page", page);
    }
    
    /**
     * append_to_response 파라미터 추가 (여러 정보 한 번에 요청)
     */
    public TmdbUrlBuilder appendToResponse(String... fields) {
        if (fields != null && fields.length > 0) {
            return param("append_to_response", String.join(",", fields));
        }
        return this;
    }

    /**
     * 최종 URL 문자열 생성
     */
    public String build() {
        if (queryParams.get("api_key") == null) {
            // 기본 API 키가 설정되어 있지 않으면 추가
            queryParams.put("api_key", apiKey);
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
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    
    // 편의 메서드: 영화 목록 URL
    public String movieList(String category, int page) {
        return init()
                .path("/movie/" + category)
                .language("ko-KR")
                .page(page)
                .build();
    }
    
    // 편의 메서드: TV 시리즈 목록 URL
    public String tvList(String category, int page) {
        return init()
                .path("/tv/" + category)
                .language("ko-KR")
                .page(page)
                .build();
    }
    
    // 편의 메서드: 애니메이션 목록 URL (장르 ID 16 사용)
    public String animationList(String sortBy, int page) {
        return init()
                .path("/discover/movie")
                .param("with_genres", "16")
                .param("sort_by", sortBy)
                .language("ko-KR")
                .page(page)
                .build();
    }
    
    // 편의 메서드: 미디어 상세 정보 URL
    public String mediaDetail(String mediaType, long id) {
        return init()
                .path("/" + mediaType + "/" + id)
                .language("ko-KR")
                .appendToResponse("videos", "credits")
                .build();
    }
} 