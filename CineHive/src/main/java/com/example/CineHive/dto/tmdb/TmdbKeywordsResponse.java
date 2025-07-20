package com.example.CineHive.dto.tmdb;

import java.util.List;
import java.util.Optional;

/**
 * TMDB API의 키워드 목록 응답을 담는 DTO입니다.
 * 영화는 'keywords', TV는 'results' 필드로 응답이 오므로 이를 모두 처리합니다.
 */
public record TmdbKeywordsResponse(
        List<TmdbKeywordResponse> keywords,
        List<TmdbKeywordResponse> results
) {
    /**
     * 영화(keywords) 또는 TV(results) 응답 중 유효한 키워드 목록을 반환합니다.
     * @return 통합된 키워드 목록
     */
    public List<TmdbKeywordResponse> getUnifiedKeywords() {
        return Optional.ofNullable(keywords).orElse(results);
    }
}