package com.example.CineHive.mapper.media;

import com.example.CineHive.dto.response.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public final class MediaMapper {

    private MediaMapper() {
        // 유틸리티 클래스이므로 인스턴스화 방지
    }

    public static MediaDetailDto toMediaDetailDto(TmdbMovieDetailResponse tmdbDetail) {
        if (tmdbDetail == null) return null;
        return MediaDetailDto.builder()
                .id(tmdbDetail.getId())
                .title(tmdbDetail.getTitle())
                .originalTitle(tmdbDetail.getOriginal_title())
                .overview(tmdbDetail.getOverview())
                .releaseDate(parseLocalDate(tmdbDetail.getRelease_date()))
                .posterPath(tmdbDetail.getPoster_path())
                .backdropPath(tmdbDetail.getBackdrop_path())
                .voteAverage(tmdbDetail.getVote_average())
                .voteCount(tmdbDetail.getVote_count())
                .popularity(tmdbDetail.getPopularity())
                .isAnimation(isAnimation(tmdbDetail.getGenre_ids()))
                .genres(toGenreDtoList(tmdbDetail.getGenres()))
                .cast(toCastDtoList(tmdbDetail.getCredits()))
                .directors(toDirectorDtoList(tmdbDetail.getCredits()))
                .videos(toVideoDtoList(tmdbDetail.getVideos()))
                .images(toImageDtoList(tmdbDetail.getImages()))
                .recommendations(toMovieSummaryList(tmdbDetail.getRecommendations()))
                .similar(toMovieSummaryList(tmdbDetail.getSimilar()))
                .keywords(toKeywordDtoList(tmdbDetail.getKeywords()))
                .watchProviders(toWatchProvidersDto(tmdbDetail.getWatchProviders()))
                .build();
    }

    public static MediaDetailDto toMediaDetailDto(TmdbTvSeriesDetailResponse tmdbDetail) {
        if (tmdbDetail == null) return null;
        return MediaDetailDto.builder()
                .id(tmdbDetail.getId())
                .title(tmdbDetail.getName())
                .originalTitle(tmdbDetail.getOriginal_name())
                .overview(tmdbDetail.getOverview())
                .releaseDate(parseLocalDate(tmdbDetail.getFirst_air_date()))
                .posterPath(tmdbDetail.getPoster_path())
                .backdropPath(tmdbDetail.getBackdrop_path())
                .voteAverage(tmdbDetail.getVote_average())
                .voteCount(tmdbDetail.getVote_count())
                .popularity(tmdbDetail.getPopularity())
                .isAnimation(isAnimation(tmdbDetail.getGenre_ids()))
                .genres(toGenreDtoList(tmdbDetail.getGenres()))
                .cast(toCastDtoList(tmdbDetail.getCredits()))
                .directors(toDirectorDtoList(tmdbDetail.getCredits()))
                .videos(toVideoDtoList(tmdbDetail.getVideos()))
                .images(toImageDtoList(tmdbDetail.getImages()))
                .recommendations(toTvSummaryList(tmdbDetail.getRecommendations()))
                .similar(toTvSummaryList(tmdbDetail.getSimilar()))
                .keywords(toKeywordDtoList(tmdbDetail.getKeywords()))
                .watchProviders(toWatchProvidersDto(tmdbDetail.getWatchProviders()))
                .build();
    }

    public static PagedResponse<MediaChartDto> toMovieChartPagedResponseFromTmdb(
            TmdbPagedResponse<TmdbMovieResponse> tmdbResponse, int requestedPage, int size) {

        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(requestedPage, size);
        }

        List<MediaChartDto> chartDtos = tmdbResponse.getResults().stream()
                .map(MediaMapper::toMediaChartDto) // isLiked 파라미터 제거
                .toList();

        // 생성자가 아닌 빌더를 사용하거나, AllArgsConstructor로 생성된 생성자를 사용합니다.
        return new PagedResponse<>(chartDtos, tmdbResponse.getPage(), size,
                tmdbResponse.getTotal_results(), tmdbResponse.getTotal_pages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotal_pages());
    }

    public static PagedResponse<MediaChartDto> toTvChartPagedResponseFromTmdb(
            TmdbPagedResponse<TmdbTvSeriesResponse> tmdbResponse, int requestedPage, int size) {

        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(requestedPage, size);
        }

        List<MediaChartDto> chartDtos = tmdbResponse.getResults().stream()
                .map(MediaMapper::toMediaChartDto) // isLiked 파라미터 제거
                .toList();

        return new PagedResponse<>(chartDtos, tmdbResponse.getPage(), size,
                tmdbResponse.getTotal_results(), tmdbResponse.getTotal_pages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotal_pages());
    }

    public static PagedResponse<MediaSummaryDto> toSearchPagedResponseFromTmdb(
            TmdbPagedResponse<TmdbMultiSearchResponse> tmdbResponse, int requestedPage, int size) {

        if (tmdbResponse == null || tmdbResponse.getResults() == null) {
            return PagedResponse.empty(requestedPage, size);
        }

        List<MediaSummaryDto> summaryDtos = tmdbResponse.getResults().stream()
                .filter(result -> "movie".equals(result.getMedia_type()) || "tv".equals(result.getMedia_type()))
                .map(MediaMapper::toMediaSummaryDto)
                .toList();

        return new PagedResponse<>(summaryDtos, tmdbResponse.getPage(), size,
                tmdbResponse.getTotal_results(), tmdbResponse.getTotal_pages(),
                tmdbResponse.getPage() >= tmdbResponse.getTotal_pages());
    }

    private static MediaChartDto toMediaChartDto(TmdbMovieResponse movie) {
        return MediaChartDto.builder()
                .mediaId(movie.getId())
                .title(movie.getTitle())
                .posterPath(movie.getPoster_path())
                .voteAverage(movie.getVote_average())
                .isAnimation(isAnimation(movie.getGenre_ids()))
                .build();
    }

    private static MediaChartDto toMediaChartDto(TmdbTvSeriesResponse tv) {
        return MediaChartDto.builder()
                .mediaId(tv.getId())
                .title(tv.getName())
                .posterPath(tv.getPoster_path())
                .voteAverage(tv.getVote_average())
                .isAnimation(isAnimation(tv.getGenre_ids()))
                .build();
    }

    private static MediaSummaryDto toMediaSummaryDto(TmdbMultiSearchResponse searchResult) {
        String title = "movie".equals(searchResult.getMedia_type())
                ? searchResult.getTitle()
                : searchResult.getName();

        return MediaSummaryDto.builder()
                .id(searchResult.getId())
                .title(title)
                .posterPath(searchResult.getPoster_path())
                .voteAverage(searchResult.getVote_average())
                .isAnimation(isAnimation(searchResult.getGenre_ids()))
                .build();
    }

    // --- 상세 정보의 하위 리스트 매핑 메서드 (변경 없음) ---
    // (이하 toGenreDtoList, toCastDtoList 등의 메서드는 이전과 동일합니다)

    private static List<GenreDto> toGenreDtoList(List<TmdbGenreResponse> genres) {
        if (genres == null) return Collections.emptyList();
        return genres.stream().map(g -> new GenreDto(g.getId(), g.getName())).toList();
    }

    private static List<CastDto> toCastDtoList(TmdbCreditsResponse credits) {
        if (credits == null || credits.getCast() == null) return Collections.emptyList();
        return credits.getCast().stream()
                .limit(10)
                .map(c -> new CastDto(c.getId(), c.getName(), c.getCharacter(), c.getProfile_path()))
                .toList();
    }

    private static List<DirectorDto> toDirectorDtoList(TmdbCreditsResponse credits) {
        if (credits == null || credits.getCrew() == null) return Collections.emptyList();
        return credits.getCrew().stream()
                .filter(c -> "Director".equals(c.getJob()))
                .map(c -> new DirectorDto(c.getId(), c.getName(), c.getProfile_path()))
                .toList();
    }

    private static List<VideoDto> toVideoDtoList(TmdbVideosResponse videos) {
        if (videos == null || videos.getResults() == null) return Collections.emptyList();
        return videos.getResults().stream()
                .map(v -> new VideoDto(v.getName(), v.getKey(), v.getSite(), v.getType()))
                .toList();
    }

    private static List<ImageDto> toImageDtoList(TmdbImagesResponse images) {
        if (images == null || images.getBackdrops() == null) return Collections.emptyList();
        return images.getBackdrops().stream()
                .limit(10)
                .map(i -> new ImageDto(i.getFile_path(), i.getAspect_ratio(), i.getHeight(), i.getWidth(), i.getVote_average(), i.getVote_count()))
                .toList();
    }

    private static List<MediaSummaryDto> toMovieSummaryList(TmdbPagedResponse<TmdbMovieResponse> response) {
        if (response == null || response.getResults() == null) return Collections.emptyList();
        return response.getResults().stream()
                .limit(10)
                .map(movie -> MediaSummaryDto.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .posterPath(movie.getPoster_path())
                        .voteAverage(movie.getVote_average())
                        .isAnimation(isAnimation(movie.getGenre_ids()))
                        .build())
                .toList();
    }

    private static List<MediaSummaryDto> toTvSummaryList(TmdbPagedResponse<TmdbTvSeriesResponse> response) {
        if (response == null || response.getResults() == null) return Collections.emptyList();
        return response.getResults().stream()
                .limit(10)
                .map(tv -> MediaSummaryDto.builder()
                        .id(tv.getId())
                        .title(tv.getName())
                        .posterPath(tv.getPoster_path())
                        .voteAverage(tv.getVote_average())
                        .isAnimation(isAnimation(tv.getGenre_ids()))
                        .build())
                .toList();
    }

    private static List<KeywordDto> toKeywordDtoList(TmdbKeywordsResponse keywords) {
        if (keywords == null) return Collections.emptyList();
        List<TmdbKeywordResponse> keywordList = Optional.ofNullable(keywords.getKeywords()).orElse(keywords.getResults());
        if (keywordList == null) return Collections.emptyList();
        return keywordList.stream()
                .map(k -> new KeywordDto(k.getId(), k.getName()))
                .toList();
    }

    private static WatchProvidersDto toWatchProvidersDto(TmdbWatchProvidersResponse providers) {
        if (providers == null || providers.getResults() == null) return new WatchProvidersDto();
        TmdbCountryWatchProvidersResponse countryProviders = providers.getResults().get("KR");
        if (countryProviders == null) return new WatchProvidersDto();
        return new WatchProvidersDto(
                countryProviders.getLink(),
                toProviderDtoList(countryProviders.getFlatrate()),
                toProviderDtoList(countryProviders.getRent()),
                toProviderDtoList(countryProviders.getBuy())
        );
    }

    private static List<ProviderDto> toProviderDtoList(List<TmdbProviderResponse> providers) {
        if (providers == null) return Collections.emptyList();
        return providers.stream()
                .map(p -> new ProviderDto(p.getProvider_id(), p.getProvider_name(), p.getLogo_path(), p.getDisplay_priority()))
                .toList();
    }


    // --- 유틸리티 메서드 ---

    private static boolean isAnimation(List<Long> genreIds) {
        return genreIds != null && genreIds.contains(16L);
    }

    private static LocalDate parseLocalDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date string: {}", dateString);
            return null;
        }
    }
}