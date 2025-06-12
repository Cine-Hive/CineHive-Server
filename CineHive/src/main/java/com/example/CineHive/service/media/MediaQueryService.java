package com.example.CineHive.service.media;

import com.example.CineHive.dto.response.MediaChartDto;
import com.example.CineHive.dto.response.MediaDetailDto;
import com.example.CineHive.dto.response.MediaSummaryDto;
import com.example.CineHive.dto.response.PagedResponse;

public interface MediaQueryService {
    MediaDetailDto getMediaDetail(Long id, String mediaType);

    // 영화 차트 메서드
    PagedResponse<MediaChartDto> getPopularMovieChart(int page, int size);
    PagedResponse<MediaChartDto> getTopRatedMovieChart(int page, int size);
    PagedResponse<MediaChartDto> getUpcomingMovieChart(int page, int size);
    PagedResponse<MediaChartDto> getNowPlayingMovieChart(int page, int size);

    // TV 차트 메서드
    PagedResponse<MediaChartDto> getPopularTvSeriesChart(int page, int size);
    PagedResponse<MediaChartDto> getTopRatedTvSeriesChart(int page, int size);
    PagedResponse<MediaChartDto> getOnTheAirTvSeriesChart(int page, int size);
    PagedResponse<MediaChartDto> getAiringTodayTvSeriesChart(int page, int size);

    // 애니메이션 차트 메서드
    PagedResponse<MediaChartDto> getPopularAnimationMovieChart(int page, int size);
    PagedResponse<MediaChartDto> getTopRatedAnimationMovieChart(int page, int size);
    PagedResponse<MediaChartDto> getPopularAnimationTvSeriesChart(int page, int size);
    PagedResponse<MediaChartDto> getTopRatedAnimationTvSeriesChart(int page, int size);

    // 검색
    PagedResponse<MediaSummaryDto> searchMedia(String query, int page, int size);
}