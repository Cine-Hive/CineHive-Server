package com.example.CineHive.dto.media;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ChartProperties {
    @Builder.Default
    private final String sortBy = "popularity.desc";
    private final String genreId;
    private final String companyId;
    private final String originCountry;
    private final String timeWindow;
    private final String keywordId;
    private final String networkId;
    private final String withOriginalLanguage;

    // --- 신규 필터 필드 추가 ---
    private final String releaseDateGte; // ~부터 (Greater than or equal)
    private final String releaseDateLte; // ~까지 (Less than or equal)
    private final String firstAirDateGte;
    private final String firstAirDateLte;
    private final String withCast; // 출연진 ID
    private final String numberOfSeasons; // 시즌 수
}