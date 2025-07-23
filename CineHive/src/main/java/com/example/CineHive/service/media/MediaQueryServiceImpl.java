package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.admin.HomeChartSettingResponse;
import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.media.*;
import com.example.CineHive.entity.media.MediaType;
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
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
    private final TransactionalOperator transactionalOperator;
    private static final int SUMMARY_SIZE = 10;

    @Override
    @Cacheable(value = "mediaDetails", key = "#mediaType + '_' + #id")
    public Mono<MediaDetailResponse> getMediaDetail(Long id, String mediaType) {
        MediaType type = parseMediaType(mediaType);
        log.info("{} 상세 정보 조회를 시작합니다. (ID: {})", type, id);

        Mono<MediaDetailResponse> detailMono = switch (type) {
            case MOVIE -> tmdbApiClient.getMovieDetail(id).map(MediaMapper::toDetailResponse);
            case TV -> tmdbApiClient.getTvSeriesDetail(id).map(MediaMapper::toDetailResponse);
        };

        return detailMono.onErrorMap(e -> this.wrapClientException(e));
    }

    @Override
    @Cacheable(value = "mediaSearch", key = "#query + '_' + #page")
    public Mono<PagedResponse<MediaSummaryResponse>> searchMedia(String query, int page) {
        log.info("미디어 검색을 시작합니다. (쿼리: '{}', 페이지: {})", query, page);
        return tmdbApiClient.searchMulti(query, page)
                .map(tmdbResponse -> MediaMapper.toPagedResponse(tmdbResponse, MediaMapper::toSummaryResponse))
                .onErrorMap(e -> this.wrapClientException(e));
    }

    @Override
    public Mono<ChartSummaryResponse> getChartSummary() {
        log.info("홈 화면 차트 요약 정보 조회를 시작합니다.");

        Mono<ChartSummaryResponse> chartSummaryMono = Mono.fromCallable(() ->
                        adminHomeChartService.getHomeChartSettings().stream()
                                .map(HomeChartSettingResponse::chartType)
                                .toList()
                )
                .subscribeOn(Schedulers.boundedElastic())
                .as(transactionalOperator::transactional) // 리액티브 스트림 내에서 트랜잭션 적용
                .flatMap(summaryChartTypes -> {
                    if (summaryChartTypes.isEmpty()) {
                        log.warn("홈 화면에 설정된 차트가 없습니다.");
                        return Mono.just(new ChartSummaryResponse(List.of()));
                    }

                    List<Mono<ChartSection>> chartSectionMonos = summaryChartTypes.stream()
                            .map(this::createChartSection)
                            .toList();

                    return Mono.zip(chartSectionMonos, objects -> Arrays.stream(objects)
                                    .map(obj -> (ChartSection) obj)
                                    .collect(Collectors.toList()))
                            .map(ChartSummaryResponse::new);
                });

        return chartSummaryMono.cache(); // Mono의 결과를 캐싱
    }

    @Override
    @Cacheable(value = "curatedCharts", key = "#chartType.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getCuratedChart(ChartType chartType, int page) {
        log.info("큐레이션 차트 조회를 시작합니다. (타입: {}, 페이지: {})", chartType.name(), page);
        ChartStrategy strategy = getChartStrategy(chartType);
        return strategy.fetchChart(tmdbApiClient, page)
                .onErrorMap(e -> this.wrapClientException(e));
    }

    @Override
    @Cacheable(value = "genreCharts", key = "#mediaType + '_' + #genreId + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getGenreChart(String mediaType, Long genreId, int page) {
        log.info("장르별 차트 조회를 시작합니다. (미디어 타입: '{}', 장르 ID: '{}', 페이지: {})", mediaType, genreId, page);
        ChartProperties props = ChartProperties.builder()
                .genreId(String.valueOf(genreId))
                .build();
        return discoverMedia(parseMediaType(mediaType), props, page);
    }

    @Override
    @Cacheable(value = "platformCharts", key = "#platform.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartResponse>> getPlatformChart(Platform platform, int page) {
        log.info("플랫폼별 차트 조회를 시작합니다. (플랫폼: '{}', 페이지: {})", platform.name(), page);
        ChartProperties props = ChartProperties.builder()
                .networkId(String.valueOf(platform.getId()))
                .build();
        return discoverMedia(MediaType.TV, props, page);
    }

    @Override
    @Cacheable("filterMetadata")
    public Mono<FilterMetadataResponse> getFilterMetadata() {
        log.info("필터 메타데이터 조회를 시작합니다.");
        Mono<List<GenreOption>> movieGenres = tmdbApiClient.getMovieGenres()
                .map(res -> res.genres().stream().map(g -> new GenreOption(g.id().longValue(), g.name())).toList());
        Mono<List<GenreOption>> tvGenres = tmdbApiClient.getTvGenres()
                .map(res -> res.genres().stream().map(g -> new GenreOption(g.id().longValue(), g.name())).toList());
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
                .onErrorMap(e -> this.wrapClientException(e));
    }


    private Mono<PagedResponse<MediaChartResponse>> discoverMedia(MediaType type, ChartProperties properties, int page) {
        log.debug("{} 미디어 탐색을 시작합니다. (속성: {}, 페이지: {})", type, properties, page);
        Mono<PagedResponse<MediaChartResponse>> discoveredMedia = switch (type) {
            case MOVIE -> tmdbApiClient.discoverMovies(page, properties)
                    .map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
            case TV -> tmdbApiClient.discoverTvSeries(page, properties)
                    .map(res -> MediaMapper.toChartPagedResponse(res, MediaMapper::toSummaryResponse));
        };

        return discoveredMedia.onErrorMap(e -> this.wrapClientException(e));
    }

    private Mono<ChartSection> createChartSection(ChartType chartType) {
        return getCuratedChart(chartType, 1)
                .map(pagedResponse -> pagedResponse.content().stream().limit(SUMMARY_SIZE).toList())
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
        log.error("TMDB API 클라이언트에서 오류가 발생했습니다.", e);
        return new BusinessException("TMDB API 오류: " + e.getMessage(), ErrorCode.TMDB_API_ERROR);
    }
}