package com.example.CineHive.domain.media.service;

import com.example.CineHive.client.tmdb.TmdbApiClient;
import com.example.CineHive.client.tmdb.dto.TmdbMovieResponse;
import com.example.CineHive.client.tmdb.dto.TmdbPagedResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesResponse;
import com.example.CineHive.domain.admin.AdminHomeChartService;
import com.example.CineHive.domain.admin.dto.HomeChartSettingResponse;
import com.example.CineHive.domain.media.enums.ChartType;
import com.example.CineHive.domain.media.enums.Platform;
import com.example.CineHive.domain.media.strategy.ChartStrategy;
import com.example.CineHive.domain.media.strategy.ChartStrategyFactory;
import com.example.CineHive.domain.media.enums.MediaType;
import com.example.CineHive.global.dto.PageResponse;
import com.example.CineHive.domain.media.dto.*;
import com.example.CineHive.domain.meta.PlatformMetadataService;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaQueryServiceImpl implements MediaQueryService {

    private final TmdbApiClient tmdbApiClient;
    private final ChartStrategyFactory chartStrategyFactory;
    private final AdminHomeChartService adminHomeChartService;
    private final PlatformMetadataService platformMetadataService;

    @Value("${tmdb.default-page-size:20}")
    private int tmdbDefaultPageSize;
    private static final int SUMMARY_SIZE = 10;

    @Override
    @Cacheable(value = "mediaDetails", key = "#mediaType + '_' + #id")
    public MediaDetailResponse getMediaDetail(Long id, String mediaType) {
        MediaType type = parseMediaType(mediaType);
        log.info("{} 상세 정보 조회를 시작합니다. (ID: {})", type, id);
        try {
            return switch (type) {
                case MOVIE -> MediaDetailResponse.from(tmdbApiClient.getMovieDetail(id));
                case TV -> MediaDetailResponse.from(tmdbApiClient.getTvSeriesDetail(id));
            };
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    @Override
    @Cacheable(value = "mediaSearch", key = "#query + '_' + #page")
    public PageResponse<MediaSummaryResponse> searchMedia(String query, int page) {
        log.info("미디어 검색을 시작합니다. (쿼리: '{}', 페이지: {})", query, page);
        try {
            var tmdbResponse = tmdbApiClient.searchMulti(query, page);
            return PageResponse.from(tmdbResponse, MediaSummaryResponse::from);
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    @Override
    public ChartSummaryResponse getChartSummary() {
        log.info("홈 화면 차트 요약 정보 조회를 시작합니다.");
        List<ChartType> summaryChartTypes = adminHomeChartService.getHomeChartSettings().stream()
                .map(HomeChartSettingResponse::chartType)
                .toList();
        if (summaryChartTypes.isEmpty()) {
            log.warn("홈 화면에 설정된 차트가 없습니다.");
            return new ChartSummaryResponse(List.of());
        }
        List<ChartSection> chartSections = summaryChartTypes.parallelStream()
                .map(this::createChartSection)
                .collect(Collectors.toList());
        return new ChartSummaryResponse(chartSections);
    }

    @Override
    @Cacheable(value = "curatedCharts", key = "#chartType.name() + '_' + #page")
    public PageResponse<MediaChartResponse> getCuratedChart(ChartType chartType, int page) {
        log.info("큐레이션 차트 조회를 시작합니다. (타입: {}, 페이지: {})", chartType.name(), page);
        ChartStrategy strategy = getChartStrategy(chartType);
        try {
            return strategy.fetchChart(tmdbApiClient, page);
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    @Override
    @Cacheable(value = "genreCharts", key = "#mediaType + '_' + #genreId + '_' + #page")
    public PageResponse<MediaChartResponse> getGenreChart(String mediaType, Long genreId, int page) {
        log.info("장르별 차트 조회를 시작합니다. (미디어 타입: '{}', 장르 ID: '{}', 페이지: {})", mediaType, genreId, page);
        ChartProperties props = ChartProperties.builder().genreId(String.valueOf(genreId)).build();
        return discoverMedia(parseMediaType(mediaType), props, page);
    }

    @Override
    @Cacheable(value = "platformCharts", key = "#platform.name() + '_' + #page")
    public PageResponse<MediaChartResponse> getPlatformChart(Platform platform, int page) {
        log.info("플랫폼별 차트 조회를 시작합니다. (플랫폼: '{}', 페이지: {})", platform.name(), page);
        ChartProperties props = ChartProperties.builder().networkId(String.valueOf(platform.getId())).build();
        return discoverMedia(MediaType.TV, props, page);
    }

    @Override
    @Cacheable("filterMetadata")
    public FilterMetadataResponse getFilterMetadata() {
        log.info("필터 메타데이터 조회를 시작합니다.");
        try {
            List<GenreOption> movieGenres = tmdbApiClient.getMovieGenres().genres().stream().map(g -> new GenreOption(g.id().longValue(), g.name())).toList();
            List<GenreOption> tvGenres = tmdbApiClient.getTvGenres().genres().stream().map(g -> new GenreOption(g.id().longValue(), g.name())).toList();
            List<PlatformOption> platforms = platformMetadataService.getPlatformOptions();
            List<SortOption> sortOptions = List.of(
                    new SortOption("popularity.desc", "인기순"),
                    new SortOption("vote_average.desc", "평점순"),
                    new SortOption("primary_release_date.desc", "최신순 (영화)"),
                    new SortOption("first_air_date.desc", "최신순 (TV)")
            );
            return FilterMetadataResponse.builder()
                    .movieGenres(movieGenres)
                    .tvGenres(tvGenres)
                    .platforms(platforms)
                    .sortOptions(sortOptions)
                    .build();
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    private PageResponse<MediaChartResponse> discoverMedia(MediaType type, ChartProperties properties, int page) {
        log.debug("{} 미디어 탐색을 시작합니다. (속성: {}, 페이지: {})", type, properties, page);
        try {
            if (type.isMovie()) {
                TmdbPagedResponse<TmdbMovieResponse> tmdbResponse = tmdbApiClient.discoverMovies(page, properties);
                return toChartResponsePage(tmdbResponse, MediaSummaryResponse::from);
            } else {
                TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse = tmdbApiClient.discoverTvSeries(page, properties);
                return toChartResponsePage(tmdbResponse, MediaSummaryResponse::from);
            }
        } catch (Exception e) {
            throw wrapClientException(e);
        }
    }

    private ChartSection createChartSection(ChartType chartType) {
        PageResponse<MediaChartResponse> pageResponse = getCuratedChart(chartType, 1);
        List<MediaChartResponse> content = pageResponse.content().stream().limit(SUMMARY_SIZE).toList();
        return ChartSection.builder()
                .chartType(chartType.name())
                .title(chartType.getDescription())
                .content(content)
                .build();
    }

    private <T> PageResponse<MediaChartResponse> toChartResponsePage(
            TmdbPagedResponse<T> tmdbResponse, Function<T, MediaSummaryResponse> mapper) {
        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PageResponse.empty();
        }
        AtomicInteger ranker = new AtomicInteger((tmdbResponse.getPage() - 1) * tmdbDefaultPageSize);
        List<MediaChartResponse> content = tmdbResponse.getResults().stream()
                .map(item -> {
                    MediaSummaryResponse summary = mapper.apply(item);
                    return MediaChartResponse.from(summary, ranker.incrementAndGet());
                })
                .toList();
        return new PageResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }

    private MediaType parseMediaType(String mediaType) {
        try {
            return MediaType.fromString(mediaType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_MEDIA_TYPE);
        }
    }

    private ChartStrategy getChartStrategy(ChartType chartType) {
        try {
            return chartStrategyFactory.getStrategy(chartType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.CHART_STRATEGY_NOT_FOUND);
        }
    }

    private BusinessException wrapClientException(Throwable e) {
        if (e instanceof BusinessException) {
            return (BusinessException) e;
        }
        log.error("TMDB API 클라이언트에서 오류가 발생했습니다.", e);
        return new BusinessException("TMDB API 오류: " + e.getMessage(), ErrorCode.TMDB_API_ERROR);
    }
}