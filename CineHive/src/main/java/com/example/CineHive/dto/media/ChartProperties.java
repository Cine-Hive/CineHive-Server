package com.example.CineHive.dto.media;

import lombok.Builder;

/**
 * TMDB의 Discover API를 위한 다양한 검색 조건을 담는 파라미터 객체입니다.
 * toBuilder=true를 통해 일부 속성만 변경된 복사본을 쉽게 생성할 수 있습니다.
 */
@Builder(toBuilder = true)
public record ChartProperties(
        String sortBy,
        String genreId,
        String companyId,
        String originCountry,
        String timeWindow,
        String keywordId,
        String networkId,
        String withOriginalLanguage,
        String releaseDateGte,
        String releaseDateLte,
        String firstAirDateGte,
        String firstAirDateLte,
        String withCast,
        String numberOfSeasons
) {
    /**
     * 비어 있는 ChartProperties 객체를 생성할 때 기본 정렬 순서를 "popularity.desc"로 설정합니다.
     */
    public static ChartProperties empty() {
        return ChartProperties.builder().sortBy("popularity.desc").build();
    }
}