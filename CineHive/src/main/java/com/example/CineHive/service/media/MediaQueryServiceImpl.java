package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.response.*;
import com.example.CineHive.dto.media.MediaType;
import com.example.CineHive.mapper.media.MediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaQueryServiceImpl implements MediaQueryService {

    private final TmdbApiClient tmdbApiClient;

    @Override
    public MediaDetailDto getMediaDetail(Long id, String mediaType) {
        MediaType type = MediaType.fromString(mediaType);

        if (type.isMovie()) {
            TmdbMovieDetailResponse movieDetail = tmdbApiClient.getMovieDetail(id);
            if (movieDetail == null) {
                throw new IllegalArgumentException("Movie not found with id: " + id);
            }
            return MediaMapper.toMediaDetailDto(movieDetail);
        } else { // type.isTv()
            TmdbTvSeriesDetailResponse tvDetail = tmdbApiClient.getTvSeriesDetail(id);
            if (tvDetail == null) {
                throw new IllegalArgumentException("TV series not found with id: " + id);
            }
            return MediaMapper.toMediaDetailDto(tvDetail);
        }
    }

    @Override
    public PagedResponse<MediaChartDto> getPopularMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.getPopularMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getTopRatedMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.getTopRatedMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getUpcomingMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.getUpcomingMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getNowPlayingMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.getNowPlayingMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getPopularTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.getPopularTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getTopRatedTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.getTopRatedTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getOnTheAirTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.getOnTheAirTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getAiringTodayTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.getAiringTodayTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaSummaryDto> searchMedia(String query, int page, int size) {
        List<TmdbMultiSearchResponse> searchResults = tmdbApiClient.searchMulti(query, page);
        return MediaMapper.toSearchPagedResponse(searchResults, page, size);
    }

    // 애니메이션 차트 메서드들
    @Override
    public PagedResponse<MediaChartDto> getPopularAnimationMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.discoverAnimationMovies(page, "popularity.desc");
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getTopRatedAnimationMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.discoverAnimationMovies(page, "vote_average.desc");
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getNowPlayingAnimationMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.discoverNowPlayingAnimationMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getUpcomingAnimationMovieChart(int page, int size) {
        TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.discoverUpcomingAnimationMovies(page);
        return MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getPopularAnimationTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.discoverAnimationTvSeries(page, "popularity.desc");
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getTopRatedAnimationTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.discoverAnimationTvSeries(page, "vote_average.desc");
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getOnTheAirAnimationTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.discoverOnTheAirAnimationTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }

    @Override
    public PagedResponse<MediaChartDto> getUpcomingAnimationTvSeriesChart(int page, int size) {
        TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.discoverUpcomingAnimationTvSeries(page);
        return MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, size);
    }
}