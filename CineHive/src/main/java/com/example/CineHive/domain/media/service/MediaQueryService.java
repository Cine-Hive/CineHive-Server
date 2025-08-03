package com.example.CineHive.domain.media.service;

import com.example.CineHive.domain.media.dto.*;
import com.example.CineHive.domain.common.dto.PageResponse;

/**
 * TMDB API를 통해 영화 및 TV 시리즈 정보를 조회하는 서비스 인터페이스입니다.
 * 모든 메서드는 동기(blocking) 방식으로 동작합니다.
 */
public interface MediaQueryService {

    /**
     * 미디어의 상세 정보를 조회합니다.
     * @param id 미디어의 TMDB ID
     * @param mediaType 미디어 타입 ("movie" 또는 "tv")
     * @return 상세 정보를 담은 MediaDetailResponse 객체
     */
    MediaDetailResponse getMediaDetail(Long id, String mediaType);

    /**
     * 주어진 검색어로 미디어를 검색합니다.
     * @param query 검색어
     * @param page 조회할 페이지 번호
     * @return 검색 결과 목록을 담은 PagedResponse 객체
     */
    PageResponse<MediaSummaryResponse> searchMedia(String query, int page);

    /**
     * 홈 화면용 차트 요약 목록을 조회합니다.
     * @return 차트 섹션 목록을 담은 ChartSummaryResponse 객체
     */
    ChartSummaryResponse getChartSummary();

    /**
     * 큐레이션 차트를 조회합니다.
     * @param chartType 조회할 차트 종류 (Enum)
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse 객체
     */
    PageResponse<MediaChartResponse> getCuratedChart(ChartType chartType, int page);

    /**
     * 특정 장르의 미디어 목록을 조회합니다.
     * @param mediaType "movie" 또는 "tv"
     * @param genreId 장르 ID
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse 객체
     */
    PageResponse<MediaChartResponse> getGenreChart(String mediaType, Long genreId, int page);

    /**
     * 특정 스트리밍 플랫폼의 TV 시리즈 목록을 조회합니다.
     * @param platform 플랫폼 Enum
     * @param page 페이지 번호
     * @return 차트 정보 목록을 담은 PagedResponse 객체
     */
    PageResponse<MediaChartResponse> getPlatformChart(Platform platform, int page);

    /**
     * 필터 UI 구성을 위한 메타데이터를 조회합니다.
     * @return 장르, 플랫폼, 정렬 옵션 목록을 담은 FilterMetadataResponse 객체
     */
    FilterMetadataResponse getFilterMetadata();
}