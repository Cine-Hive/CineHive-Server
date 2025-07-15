package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.entity.setting.HomeChartSetting;
import com.example.CineHive.dto.media.ChartProperties;
import com.example.CineHive.dto.media.ChartType;
import com.example.CineHive.dto.media.MediaType;
import com.example.CineHive.dto.media.Platform;
import com.example.CineHive.dto.response.*;
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
    public Mono<MediaDetailDto> getMediaDetail(Long id, String mediaType) {
        MediaType type = MediaType.fromString(mediaType);
        log.info("Fetching detail for {} with id {}", type, id);

        return switch (type) {
            case MOVIE -> tmdbApiClient.getMovieDetail(id).map(MediaMapper::toMediaDetailDto);
            case TV -> tmdbApiClient.getTvSeriesDetail(id).map(MediaMapper::toMediaDetailDto);
        };
    }

    @Override
    @Cacheable(value = "mediaSearch", key = "#query + '_' + #page")
    public Mono<PagedResponse<MediaSummaryDto>> searchMedia(String query, int page) {
        log.info("Searching media for query '{}' on page {}", query, page);
        return tmdbApiClient.searchMulti(query, page)
                .map(tmdbResponse -> MediaMapper.toSearchPagedResponseFromTmdb(tmdbResponse, page, DEFAULT_PAGE_SIZE));
    }

    @Override
    @Cacheable("chartSummary")
    public Mono<ChartSummaryResponse> getChartSummary() {
        log.info("Fetching chart summary for home screen from database settings.");
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
    public Mono<PagedResponse<MediaChartDto>> getCuratedChart(ChartType chartType, int page) {
        log.info("Fetching curated chart for {} on page {}", chartType.name(), page);
        ChartStrategy strategy = chartStrategyFactory.getStrategy(chartType);
        return strategy.fetchChart(tmdbApiClient, page);
    }

    @Override
    @Cacheable(value = "genreCharts", key = "#mediaType + '_' + #genreId + '_' + #page")
    public Mono<PagedResponse<MediaChartDto>> getGenreChart(String mediaType, Long genreId, int page) {
        log.info("Fetching genre chart for mediaType '{}', genreId '{}' on page {}", mediaType, genreId, page);
        ChartProperties props = ChartProperties.builder()
                .genreId(String.valueOf(genreId))
                .sortBy("popularity.desc")
                .build();
        return discoverMedia(MediaType.fromString(mediaType), props, page);
    }

    @Override
    @Cacheable(value = "platformCharts", key = "#platform.name() + '_' + #page")
    public Mono<PagedResponse<MediaChartDto>> getPlatformChart(Platform platform, int page) {
        log.info("Fetching platform chart for platform '{}' on page {}", platform.name(), page);
        ChartProperties props = ChartProperties.builder()
                .networkId(String.valueOf(platform.getId()))
                .sortBy("popularity.desc")
                .build();
        return discoverMedia(MediaType.TV, props, page);
    }

    @Override
    @Cacheable("filterMetadata")
    public Mono<FilterMetadataResponse> getFilterMetadata() {
        log.info("Fetching filter metadata.");
        Mono<List<GenreOptionDto>> movieGenres = tmdbApiClient.getMovieGenres()
                .map(res -> res.getGenres().stream().map(g -> new GenreOptionDto(g.getId(), g.getName())).toList());
        Mono<List<GenreOptionDto>> tvGenres = tmdbApiClient.getTvGenres()
                .map(res -> res.getGenres().stream().map(g -> new GenreOptionDto(g.getId(), g.getName())).toList());

        Mono<List<PlatformOptionDto>> platforms = platformMetadataService.getPlatformOptions();

        Mono<List<SortOptionDto>> sortOptions = Mono.just(List.of(
                new SortOptionDto("popularity.desc", "인기순"),
                new SortOptionDto("vote_average.desc", "평점순"),
                new SortOptionDto("primary_release_date.desc", "최신순 (영화)"),
                new SortOptionDto("first_air_date.desc", "최신순 (TV)")
        ));

        return Mono.zip(movieGenres, tvGenres, platforms, sortOptions)
                .map(tuple -> FilterMetadataResponse.builder()
                        .movieGenres(tuple.getT1())
                        .tvGenres(tuple.getT2())
                        .platforms(tuple.getT3())
                        .sortOptions(tuple.getT4())
                        .build());
    }

    private Mono<PagedResponse<MediaChartDto>> discoverMedia(MediaType type, ChartProperties properties, int page) {
        log.debug("Discovering {} with properties {} on page {}", type, properties, page);
        return switch (type) {
            case MOVIE -> tmdbApiClient.discoverMovies(page, properties)
                    .map(res -> MediaMapper.toMovieChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
            case TV -> tmdbApiClient.discoverTvSeries(page, properties)
                    .map(res -> MediaMapper.toTvChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
        };
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
}