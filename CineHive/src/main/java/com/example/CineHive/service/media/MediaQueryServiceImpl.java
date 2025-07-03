package com.example.CineHive.service.media;

import com.example.CineHive.client.TmdbApiClient;
import com.example.CineHive.dto.media.ChartProperties;
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
        log.info("Fetching chart summary for home screen.");
        // 홈 화면에 보여줄 차트 목록 (DB나 설정 파일에서 관리 가능)
        List<ChartType> summaryChartTypes = List.of(
                ChartType.TRENDING_MOVIES_WEEK,
                ChartType.TOP_RATED_TV,
                ChartType.KOREAN_DRAMA_SERIES,
                ChartType.PIXAR_ANIMATION_COLLECTION
        );

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
    @Cacheable(value = "platformCharts", key = "#networkId + '_' + #page")
    public Mono<PagedResponse<MediaChartDto>> getPlatformChart(Long networkId, int page) {
        log.info("Fetching platform chart for networkId '{}' on page {}", networkId, page);
        ChartProperties props = ChartProperties.builder()
                .networkId(String.valueOf(networkId))
                .sortBy("popularity.desc")
                .build();
        // 플랫폼 차트는 TV 시리즈만 해당
        return discoverMedia(MediaType.TV, props, page);
    }

    @Override
    @Cacheable("filterMetadata")
    public Mono<FilterMetadataResponse> getFilterMetadata() {
        log.info("Fetching filter metadata.");
        Mono<List<GenreOptionDto>> movieGenres = tmdbApiClient.getMovieGenres()
                .map(res -> res.getGenres().stream()
                        .map(g -> new GenreOptionDto(g.getId(), g.getName()))
                        .toList());

        Mono<List<GenreOptionDto>> tvGenres = tmdbApiClient.getTvGenres()
                .map(res -> res.getGenres().stream()
                        .map(g -> new GenreOptionDto(g.getId(), g.getName()))
                        .toList());

        Mono<List<PlatformOptionDto>> platforms = Mono.just(List.of(
                new PlatformOptionDto(213L, "Netflix", "/t2yyOv4xD9xpcGPNavKrDdGFEly.jpg"),
                new PlatformOptionDto(2739L, "Disney+", "/uzKjDo45H33D4nJ2T2aC2L8b20.jpg"),
                new PlatformOptionDto(1024L, "Amazon Prime Video", "/emthSpie82kbr2s4fM0M3aL2h29.jpg"),
                new PlatformOptionDto(49L, "HBO", "/tuomPhY2UtuPTqqFnKMVHvroqBA.jpg"),
                new PlatformOptionDto(2552L, "Apple TV+", "/4f3T3Z1yK2dYvKaS3d2p2y9N2B.jpg"),
                new PlatformOptionDto(453L, "Hulu", "/pqUTCleNUiTLAVaH28p3OP_2hA.jpg"),
                new PlatformOptionDto(3321L, "Wavve", "/1TB2a264J0gds6Teyvvr9a46L9E.jpg"),
                new PlatformOptionDto(318L, "tvN", "/kGRavMqU4Oad2b2Hza53v2d2jaA.jpg"),
                new PlatformOptionDto(67L, "SBS", "/j61aM2N2dK3mOo4L1so2pA14T3A.jpg"),
                new PlatformOptionDto(62L, "KBS", "/11G3GzYg3g2iT3aB2i2b0O6om3.jpg"),
                new PlatformOptionDto(74L, "MBC", "/wK2g6sY2yAl266eP3w5epDkw5dG.jpg"),
                new PlatformOptionDto(269L, "JTBC", "/sL43iR2nESpgh1g3d7s2iHw3Gz.jpg")
        ));

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

    /**
     * 모든 필터 기반 조회의 중심이 되는 비공개 메서드.
     * 외부에서는 직접 호출하지 않고, getGenreChart, getPlatformChart 등에서 재사용합니다.
     *
     * @param type       미디어 타입 (MOVIE 또는 TV)
     * @param properties 필터 조건
     * @param page       페이지 번호
     * @return 페이지 정보가 포함된 차트 DTO의 Mono
     */
    private Mono<PagedResponse<MediaChartDto>> discoverMedia(MediaType type, ChartProperties properties, int page) {
        // 상세 로그는 debug 레벨로 남겨서 평소에는 보이지 않도록 함
        log.debug("Discovering {} with properties {} on page {}", type, properties, page);
        return switch (type) {
            case MOVIE -> tmdbApiClient.discoverMovies(page, properties)
                    .map(res -> MediaMapper.toMovieChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
            case TV -> tmdbApiClient.discoverTvSeries(page, properties)
                    .map(res -> MediaMapper.toTvChartPagedResponseFromTmdb(res, page, DEFAULT_PAGE_SIZE));
        };
    }

    /**
     * 홈 화면 요약을 위한 개별 차트 섹션을 생성하는 비공개 헬퍼 메서드.
     *
     * @param chartType 생성할 차트의 타입
     * @return 차트 섹션 DTO의 Mono
     */
    private Mono<ChartSection> createChartSection(ChartType chartType) {
        return getCuratedChart(chartType, 1) // 각 차트의 첫 페이지만 조회
                .map(pagedResponse -> pagedResponse.getContent().stream().limit(SUMMARY_SIZE).toList())
                .map(content -> ChartSection.builder()
                        .chartType(chartType.name())
                        .title(chartType.getDescription()) // Enum에 정의된 설명 사용
                        .content(content)
                        .build());
    }
}