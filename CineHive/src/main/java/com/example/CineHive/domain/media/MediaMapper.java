package com.example.CineHive.domain.media;

import com.example.CineHive.client.tmdb.dto.*;
import com.example.CineHive.domain.media.dto.*;
import com.example.CineHive.global.common.dto.PagedResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 외부 API(TMDB) 응답 객체를 내부에서 사용하는 DTO로 변환하는 유틸리티 클래스입니다.
 */
@Slf4j
public final class MediaMapper {

    private MediaMapper() {}

    public static MediaDetailResponse toDetailResponse(TmdbMovieDetailResponse tmdb) {
        if (tmdb == null) return null;
        return MediaDetailResponse.builder()
                .id(tmdb.id())
                .title(tmdb.title())
                .originalTitle(tmdb.originalTitle())
                .overview(tmdb.overview())
                .releaseDate(parseLocalDate(tmdb.releaseDate()))
                .posterPath(tmdb.posterPath())
                .backdropPath(tmdb.backdropPath())
                .voteAverage(tmdb.voteAverage())
                .voteCount(tmdb.voteCount())
                .popularity(tmdb.popularity())
                .isAnimation(isAnimation(tmdb.genres()))
                .genres(toGenreOptions(tmdb.genres()))
                .cast(toCreditResponses(tmdb.credits(), "Actor"))
                .directors(toCreditResponses(tmdb.credits(), "Director"))
                .videos(toVideoInfos(tmdb.videos()))
                .images(toImageInfos(tmdb.images()))
                .recommendations(toSummaryResponses(tmdb.recommendations(), MediaMapper::toSummaryResponse))
                .similar(toSummaryResponses(tmdb.similar(), MediaMapper::toSummaryResponse))
                .keywords(toKeywordInfos(tmdb.keywords()))
                .watchProviders(toWatchProviderInfo(tmdb.watchProviders()))
                .build();
    }

    public static MediaDetailResponse toDetailResponse(TmdbTvSeriesDetailResponse tmdb) {
        if (tmdb == null) return null;
        return MediaDetailResponse.builder()
                .id(tmdb.id())
                .title(tmdb.name())
                .originalTitle(tmdb.originalName())
                .overview(tmdb.overview())
                .releaseDate(parseLocalDate(tmdb.firstAirDate()))
                .posterPath(tmdb.posterPath())
                .backdropPath(tmdb.backdropPath())
                .voteAverage(tmdb.voteAverage())
                .voteCount(tmdb.voteCount())
                .popularity(tmdb.popularity())
                .isAnimation(isAnimation(tmdb.genres()))
                .genres(toGenreOptions(tmdb.genres()))
                .cast(toCreditResponses(tmdb.credits(), "Actor"))
                .directors(toCreditResponses(tmdb.credits(), "Director"))
                .videos(toVideoInfos(tmdb.videos()))
                .images(toImageInfos(tmdb.images()))
                .recommendations(toSummaryResponses(tmdb.recommendations(), MediaMapper::toSummaryResponse))
                .similar(toSummaryResponses(tmdb.similar(), MediaMapper::toSummaryResponse))
                .keywords(toKeywordInfos(tmdb.keywords()))
                .watchProviders(toWatchProviderInfo(tmdb.watchProviders()))
                .build();
    }

    public static <T, R> PagedResponse<R> toPagedResponse(TmdbPagedResponse<T> tmdbResponse, Function<T, R> mapper) {
        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(1, 20);
        }

        List<R> content = tmdbResponse.getResults().stream()
                .map(mapper)
                .toList();

        return new PagedResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }

    public static <T> PagedResponse<MediaChartResponse> toChartPagedResponse(
            TmdbPagedResponse<T> tmdbResponse, Function<T, MediaSummaryResponse> mapper) {

        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(1, 20);
        }

        java.util.concurrent.atomic.AtomicInteger ranker = new java.util.concurrent.atomic.AtomicInteger(
                (tmdbResponse.getPage() - 1) * 20
        );

        List<MediaChartResponse> content = tmdbResponse.getResults().stream()
                .map(item -> {
                    MediaSummaryResponse summary = mapper.apply(item);
                    return MediaChartResponse.builder()
                            .mediaId(summary.id())
                            .title(summary.title())
                            .posterPath(summary.posterPath())
                            .voteAverage(summary.voteAverage())
                            .isAnimation(summary.isAnimation())
                            .rank(ranker.incrementAndGet())
                            .build();
                })
                .toList();

        return new PagedResponse<>(
                content,
                tmdbResponse.getPage(),
                content.size(),
                (long) tmdbResponse.getTotalResults(),
                tmdbResponse.getTotalPages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotalPages()
        );
    }

    public static MediaSummaryResponse toSummaryResponse(TmdbMovieResponse movie) {
        return MediaSummaryResponse.builder()
                .id(movie.id())
                .title(movie.title())
                .posterPath(movie.posterPath())
                .voteAverage(movie.voteAverage())
                .isAnimation(isAnimationById(movie.genreIds()))
                .build();
    }

    public static MediaSummaryResponse toSummaryResponse(TmdbTvSeriesResponse tv) {
        return MediaSummaryResponse.builder()
                .id(tv.id())
                .title(tv.name())
                .posterPath(tv.posterPath())
                .voteAverage(tv.voteAverage())
                .isAnimation(isAnimationById(tv.genreIds()))
                .build();
    }

    public static MediaSummaryResponse toSummaryResponse(TmdbMultiSearchResponse multi) {
        return MediaSummaryResponse.builder()
                .id(multi.id())
                .title(multi.getUnifiedTitle())
                .posterPath(multi.posterPath())
                .voteAverage(multi.voteAverage())
                .isAnimation(isAnimationById(multi.genreIds()))
                .build();
    }

    private static List<GenreOption> toGenreOptions(List<TmdbGenreResponse> genres) {
        if (genres == null) return Collections.emptyList();
        return genres.stream()
                .map(g -> new GenreOption(g.id().longValue(), g.name()))
                .toList();
    }

    private static List<CreditResponse> toCreditResponses(TmdbCreditsResponse credits, String job) {
        if (credits == null) return Collections.emptyList();

        Stream<CreditResponse> stream = Stream.empty();
        if ("Director".equals(job) && credits.crew() != null) {
            stream = credits.crew().stream()
                    .filter(c -> "Director".equals(c.job()))
                    .map(c -> CreditResponse.builder()
                            .personId(c.id())
                            .name(c.name())
                            .job(c.job())
                            .profilePath(c.profilePath())
                            .build());
        } else if ("Actor".equals(job) && credits.cast() != null) {
            stream = credits.cast().stream()
                    .limit(10)
                    .map(c -> CreditResponse.builder()
                            .personId(c.id())
                            .name(c.name())
                            .job("Actor")
                            .character(c.character())
                            .profilePath(c.profilePath())
                            .build());
        }
        return stream.toList();
    }

    private static List<VideoInfo> toVideoInfos(TmdbVideosResponse videos) {
        if (videos == null || videos.results() == null) return Collections.emptyList();
        return videos.results().stream()
                .map(v -> new VideoInfo(v.name(), v.key(), v.site(), v.type()))
                .toList();
    }

    private static List<ImageInfo> toImageInfos(TmdbImagesResponse images) {
        if (images == null || images.backdrops() == null) return Collections.emptyList();
        return images.backdrops().stream()
                .limit(10)
                .map(i -> ImageInfo.builder()
                        .filePath(i.filePath())
                        .aspectRatio(i.aspectRatio())
                        .height(i.height())
                        .width(i.width())
                        .voteAverage(i.voteAverage())
                        .voteCount(i.voteCount())
                        .build())
                .toList();
    }

    private static <T> List<MediaSummaryResponse> toSummaryResponses(TmdbPagedResponse<T> response, Function<T, MediaSummaryResponse> mapper) {
        if (response == null || response.getResults() == null) return Collections.emptyList();
        return response.getResults().stream()
                .limit(10)
                .map(mapper)
                .toList();
    }

    private static List<KeywordInfo> toKeywordInfos(TmdbKeywordsResponse keywords) {
        if (keywords == null) return Collections.emptyList();
        return Optional.ofNullable(keywords.getUnifiedKeywords()).orElse(Collections.emptyList())
                .stream()
                .map(k -> new KeywordInfo(k.id(), k.name()))
                .toList();
    }

    private static WatchProviderInfo toWatchProviderInfo(TmdbWatchProvidersResponse providers) {
        if (providers == null || providers.results() == null) return null;
        TmdbCountryWatchProvidersResponse krProviders = providers.results().get("KR");
        if (krProviders == null) return null;

        return WatchProviderInfo.builder()
                .link(krProviders.link())
                .flatrate(toProviderOptions(krProviders.flatrate()))
                .rent(toProviderOptions(krProviders.rent()))
                .buy(toProviderOptions(krProviders.buy()))
                .build();
    }

    private static List<WatchProviderInfo.ProviderOption> toProviderOptions(List<TmdbProviderResponse> providers) {
        if (providers == null) return Collections.emptyList();
        return providers.stream()
                .map(p -> WatchProviderInfo.ProviderOption.builder()
                        .providerId(p.providerId())
                        .providerName(p.providerName())
                        .logoPath(p.logoPath())
                        .displayPriority(p.displayPriority())
                        .build())
                .toList();
    }

    private static boolean isAnimation(List<TmdbGenreResponse> genres) {
        return genres != null && genres.stream().anyMatch(g -> g.id() == 16);
    }

    private static boolean isAnimationById(List<Long> genreIds) {
        return genreIds != null && genreIds.contains(16L);
    }

    private static LocalDate parseLocalDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            log.warn("날짜 문자열 파싱 실패: {}", dateString);
            return null;
        }
    }
}