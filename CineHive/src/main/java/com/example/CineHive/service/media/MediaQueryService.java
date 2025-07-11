package com.example.CineHive.service.media;

import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.response.*;
import reactor.core.publisher.Mono;

/**
 * TMDB API를 통해 영화 및 TV 시리즈 정보를 조회하는 서비스 인터페이스.
 * 모든 메서드는 비동기(Reactive) 방식으로 동작하며 Mono를 반환합니다.
 */
public interface MediaQueryService {

    /**
     * 미디어의 상세 정보를 조회합니다.
     * @param id 미디어의 TMDB ID
     * @param mediaType 미디어 타입 ("movie" 또는 "tv")
     * @return 상세 정보를 담은 MediaDetailDto의 Mono 객체
     */
    Mono<MediaDetailDto> getMediaDetail(Long id, String mediaType);

    /**
     * 주어진 검색어로 미디어(영화, TV 통합)를 검색합니다.
     * @param query 검색어
     * @param page 조회할 페이지 번호
     * @return 검색 결과 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaSummaryDto>> searchMedia(String query, int page);

    /**
     * 서버에서 동적으로 정의한 홈 화면용 차트 요약 목록을 조회합니다.
     * @return 동적으로 구성된 차트 섹션 목록을 담은 ChartSummaryResponse의 Mono 객체
     */
    Mono<ChartSummaryResponse> getChartSummary();

    /**
     * 서버에 미리 정의된 정적/큐레이션 차트를 조회합니다.
     * @param chartType 조회할 차트 종류 (Enum)
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaChartDto>> getCuratedChart(ChartType chartType, int page);

    /**
     * 특정 장르의 미디어 목록을 조회합니다.
     * @param mediaType "movie" 또는 "tv"
     * @param genreId 장르 ID
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaChartDto>> getGenreChart(String mediaType, Long genreId, int page);

    /**
     * 특정 스트리밍 플랫폼의 TV 시리즈 목록을 조회합니다.
     * @param networkId 플랫폼(네트워크) ID
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse의 Mono 객체
     */
    Mono<PagedResponse<MediaChartDto>> getPlatformChart(Platform platform, int page);

    /**
     * 클라이언트 필터 UI 구성을 위한 메타데이터를 조회합니다.
     * @return 장르, 플랫폼, 정렬 옵션 목록을 담은 FilterMetadataResponse의 Mono 객체
     */
    Mono<FilterMetadataResponse> getFilterMetadata();
}