package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.media.MediaType;
import com.example.CineHive.dto.response.*;
import com.example.CineHive.mapper.media.MediaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaQueryServiceImpl implements MediaQueryService {

    private final TmdbApiClient tmdbApiClient;
    private static final int DEFAULT_PAGE_SIZE = 20; // TMDB 기본 페이지 크기

    @Override
    @Cacheable(value = "mediaDetails", key = "#mediaType + '_' + #id")
    public Mono<MediaDetailDto> getMediaDetail(Long id, String mediaType) {
        MediaType type = MediaType.fromString(mediaType);
        log.info("Fetching detail for {} with id {}", type, id);

        if (type.isMovie()) {
            return tmdbApiClient.getMovieDetail(id)
                    .map(MediaMapper::toMediaDetailDto);
        } else {
            return tmdbApiClient.getTvSeriesDetail(id)
                    .map(MediaMapper::toMediaDetailDto);
        }
    }

    @Override
    @Cacheable(value = "mediaCharts", key = "#chartType.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartDto>> getChart(ChartType chartType, int page) {
        log.info("Fetching chart for {} on page {}", chartType.name(), page);

        if (chartType.getMediaType().isMovie()) {
            Mono<TmdbPagedResponse<TmdbMovieResponse>> responseMono = switch (chartType) {
                // ... (switch 케이스는 동일)
                case POPULAR_MOVIES -> tmdbApiClient.getPopularMovies(page);
                case TOP_RATED_MOVIES -> tmdbApiClient.getTopRatedMovies(page);
                case UPCOMING_MOVIES -> tmdbApiClient.getUpcomingMovies(page);
                case NOW_PLAYING_MOVIES -> tmdbApiClient.getNowPlayingMovies(page);
                case POPULAR_ANIMATION_MOVIES -> tmdbApiClient.discoverAnimationMovies(page, "popularity.desc");
                case TOP_RATED_ANIMATION_MOVIES -> tmdbApiClient.discoverAnimationMovies(page, "vote_average.desc");
                case NOW_PLAYING_ANIMATION_MOVIES -> tmdbApiClient.discoverNowPlayingAnimationMovies(page);
                case UPCOMING_ANIMATION_MOVIES -> tmdbApiClient.discoverUpcomingAnimationMovies(page);
                default -> Mono.error(new IllegalArgumentException("Unsupported movie chart type: " + chartType));
            };
            return responseMono.map(tmdbResponse ->
                    MediaMapper.toMovieChartPagedResponseFromTmdb(tmdbResponse, page, DEFAULT_PAGE_SIZE)
            );
        } else { // TV Series
            Mono<TmdbPagedResponse<TmdbTvSeriesResponse>> responseMono = switch (chartType) {
                // ... (switch 케이스는 동일)
                case POPULAR_TV -> tmdbApiClient.getPopularTvSeries(page);
                case TOP_RATED_TV -> tmdbApiClient.getTopRatedTvSeries(page);
                case ON_THE_AIR_TV -> tmdbApiClient.getOnTheAirTvSeries(page);
                case AIRING_TODAY_TV -> tmdbApiClient.getAiringTodayTvSeries(page);
                case POPULAR_ANIMATION_TV -> tmdbApiClient.discoverAnimationTvSeries(page, "popularity.desc");
                case TOP_RATED_ANIMATION_TV -> tmdbApiClient.discoverAnimationTvSeries(page, "vote_average.desc");
                default -> Mono.error(new IllegalArgumentException("Unsupported TV chart type: " + chartType));
            };
            return responseMono.map(tmdbResponse ->
                    MediaMapper.toTvChartPagedResponseFromTmdb(tmdbResponse, page, DEFAULT_PAGE_SIZE)
            );
        }
    }

    @Override
    @Cacheable(value = "mediaSearch", key = "#query + '_' + #page")
    public Mono<PagedResponse<MediaSummaryDto>> searchMedia(String query, int page) {
        log.info("Searching media for query '{}' on page {}", query, page);
        return tmdbApiClient.searchMulti(query, page)
                .map(tmdbResponse -> MediaMapper.toSearchPagedResponseFromTmdb(tmdbResponse, page, DEFAULT_PAGE_SIZE));
    }
}