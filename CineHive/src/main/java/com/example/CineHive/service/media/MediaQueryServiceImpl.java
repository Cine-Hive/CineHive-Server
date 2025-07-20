package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.media.*;
import com.example.CineHive.dto.response.MediaChartResponse;
import com.example.CineHive.entity.setting.HomeChartSetting;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.media.MediaMapper;
import com.example.CineHive.service.admin.AdminHomeChartService;
import com.example.CineHive.service.meta.PlatformMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
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
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int SUMMARY_SIZE = 10;

    @Override
    @Cacheable(value = "mediaDetails", key = "#mediaType + '_' + #id")
    public Mono<MediaDetailResponse> getMediaDetail(Long id, String mediaType) {
        MediaType type = parseMediaType(mediaType);
        log.info("{} 상세 정보 조회 시작 (ID: {})", type, id);

        Mono<MediaDetailResponse> detailMono = switch (type) {
            case MOVIE -> tmdbApiClient.getMovieDetail(id).map(MediaMapper::toMediaDetailDto);
            case TV -> tmdbApiClient.getTvSeriesDetail(id).map(MediaMapper::toMediaDetailDto);
        };

        return detailMono.onErrorMap(this::wrapClientException);
    }

    @Override
    @Cacheable(value = "mediaSearch", key = "#query + '_' + #page")
    public Mono<PagedResponse<MediaSummaryResponse>> searchMedia(String query, int page) {
        log.info("미디어 검색 시작 (쿼리: '{}', 페이지: {})", query, page);
        return tmdbApiClient.searchMulti(query, page)
                .map(tmdbResponse -> MediaMapper.toSearchPagedResponseFromTmdb(tmdbResponse, page, DEFAULT_PAGE_SIZE))
                .onErrorMap(this::wrapClientException);
    }

    @Override
    @Cacheable("chartSummary")
    public Mono<ChartSummaryResponse> getChartSummary() {
        log.info("홈 화면 차트 요약 정보 조회 시작 (DB 설정 기반)");
        List<ChartType> summaryChartTypes = adminHomeChartService.getHomeChartSettings().stream()
                .map(HomeChartSetting::getChartType)
                .toList();
        if (summaryChartTypes.isEmpty()) {
            return Mono.just(new ChartSummaryResponse(List.of()));
        }
        List<Mono<ChartSection>> chartSectionMonos = summaryChartTypes.stream()
                .map(this::createChartSection)
                .toList();
        return Mono.zip(chartSectionMonos, objects -> Arrays.stream(objects)
                        .map(obj -> (ChartSection) obj)
                        .collect(Collectors.toList()))
                .map(ChartSummaryResponse::new);
    }

    @Override
    @Cacheable(value = "curatedCharts", key = "#chartType.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getCuratedChart(ChartType chartType, int page) {
        log.info("큐레이션 차트 조회 시작 (타입: {}, 페이지: {})", chartType.name(), page);
        ChartStrategy strategy = getChartStrategy(chartType);
        return strategy.fetchChart(tmdbApiClient, page)
                .onErrorMap(this::wrapClientException);
    }

    @Override
    @Cacheable(value = "genreCharts", key = "#mediaType + '_' + #genreId + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getGenreChart(String mediaType, Long genreId, int page) {
        log.info("장르별 차트 조회 시작 (미디어 타입: '{}', 장르 ID: '{}', 페이지: {})", mediaType, genreId, page);
        ChartProperties props = ChartProperties.builder()
                .genreId(String.valueOf(genreId))
                .sortBy("popularity.desc")
                .build();
        return discoverMedia(parseMediaType(mediaType), props, page);
    }

    @Override
    @Cacheable(value = "platformCharts", key = "#platform.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getPlatformChart(Platform platform, int page) {
        log.info("플랫폼별 차트 조회 시작 (플랫폼: '{}', 페이지: {})", platform.name(), page);
        ChartProperties props = ChartProperties.builder()
                .networkId(String.valueOf(platform.getId()))
                .sortBy("popularity.desc")
                .build();
        return discoverMedia(MediaType.TV, props, page);
    }

    @Override
    @Cacheable("filterMetadata")
    public Mono<FilterMetadataResponse> getFilterMetadata() {
        log.info("필터 메타데이터 조회 시작");
        Mono<List<GenreOption>> movieGenres = tmdbApiClient.getMovieGenres()
                .map(res -> res.getGenres().stream().map(g -> new GenreOption(g.getId(), g.getName())).toList());
        Mono<List<GenreOption>> tvGenres = tmdbApiClient.getTvGenres()
                .map(res -> res.getGenres().stream().map(g -> new GenreOption(g.getId(), g.getName())).toList());
        Mono<List<PlatformOption>> platforms = platformMetadataService.getPlatformOptions();
        Mono<List<SortOption>> sortOptions = Mono.just(List.of(
                new SortOption("popularity.desc", "인기순"),
                new SortOption("vote_average.desc", "평점순"),
                new SortOption("primary_release_date.desc", "최신순 (영화)"),
                new SortOption("first_air_date.desc", "최신순 (TV)")
        ));

        return Mono.zip(movieGenres, tvGenres, platforms, sortOptions)
                .map(tuple -> FilterMetadataResponse.builder()
                        .movieGenres(tuple.getT1())
                        .tvGenres(tuple.getT2())
                        .platforms(tuple.getT3())
                        .sortOptions(tuple.getT4())
                        .build())
                .onErrorMap(this::wrapClientException);
    }

    //== Private Helper Methods ==//

    private Mono<PagedResponse<MediaChartResponse>> discoverMedia(MediaType type, ChartProperties properties, int page) {
        log.debug("{} 탐색 시작 (속성: {}, 페이지: {})", type, properties, page);
        Mono<PagedResponse<MediaChartResponse>> discoveredMedia = switch (type) {
            case MOVIE -> tmdbApiClient.discoverMovies(page, properties)
                    .map(res -> MediaMapper.toMovieChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
            case TV -> tmdbApiClient.discoverTvSeries(page, properties)
                    .map(res -> MediaMapper.toTvChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
        };
        return discoveredMedia.onErrorMap(this::wrapClientException);
    }

    private Mono<ChartSection> createChartSection(ChartType chartType) {
        return getCuratedChart(chartType, 1)
                .map(pagedResponse -> pagedResponse.getContent().stream().limit(SUMMARY_SIZE).toList())
                .map(content -> ChartSection.builder()
                        .chartType(chartType.name())
                        .title(chartType.getDescription())
                        .content(content)
                        .build());
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
        log.error("TMDB API 클라이언트 오류 발생", e);
        return new BusinessException("TMDB API Error: " + e.getMessage(), ErrorCode.TMDB_API_ERROR);
    }
}
