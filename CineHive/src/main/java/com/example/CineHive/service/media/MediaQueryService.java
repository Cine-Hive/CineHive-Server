package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.response.MediaChartDto;
import com.example.CineHive.dto.response.MediaDetailDto;
import com.example.CineHive.dto.response.MediaSummaryDto;
import com.example.CineHive.dto.response.PagedResponse;
import reactor.core.publisher.Mono;

/**
 * TMDB API를 통해 영화 및 TV 시리즈 정보를 조회하는 서비스 인터페이스.
 * 모든 메서드는 비동기(Reactive) 방식으로 동작하며 Mono를 반환합니다.
 */
// MediaQueryService.java

public interface MediaQueryService {

    // ... (getMediaDetail은 변경 없음)
    Mono<MediaDetailDto> getMediaDetail(Long id, String mediaType);

    /**
     * 지정된 타입의 차트 정보를 페이지 단위로 조회합니다.
     * 한 페이지당 아이템 수는 TMDB 기본값(20개)을 따릅니다.
     *
     * @param chartType 조회할 차트의 종류 (Enum)
     * @param page      조회할 페이지 번호 (1-based)
     * @return 차트 정보 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaChartDto>> getChart(ChartType chartType, int page);

    /**
     * 주어진 검색어로 미디어(영화, TV 통합)를 검색합니다.
     *
     * @param query 검색어
     * @param page  조회할 페이지 번호 (1-based)
     * @return 검색 결과 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaSummaryDto>> searchMedia(String query, int page);

}