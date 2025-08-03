package com.example.CineHive.domain.media;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.domain.common.dto.PageResponse;
import com.example.CineHive.domain.media.dto.MediaChartResponse;

@FunctionalInterface
public interface ChartStrategy {
    /**
     * 특정 차트의 데이터를 가져오는 전략을 정의합니다.
     * @param apiClient TMDB API 클라이언트
     * @param page      요청할 페이지 번호
     * @return 페이징된 MediaChartResponse
     */
    PageResponse<MediaChartResponse> fetchChart(TmdbApiClient client, int page);
}