package com.example.CineHive.mapper.media;

import com.example.CineHive.dto.response.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class MediaMapper {

    /**
     * TMDB 영화 상세 응답을 MediaDetailDto로 변환
     */
    public static MediaDetailDto toMediaDetailDto(TmdbMovieDetailResponse tmdbDetail) {
        // 장르 변환
        List<GenreDto> genres = tmdbDetail.getGenres().stream()
                .map(MediaMapper::toGenreDto)
                .toList();

        // 캐스트 변환 (상위 10명만)
        List<CastDto> cast = tmdbDetail.getCredits().getCast().stream()
                .limit(10)
                .map(MediaMapper::toCastDto)
                .toList();

        // 감독 변환
        List<DirectorDto> directors = tmdbDetail.getCredits().getCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob()))
                .map(MediaMapper::toDirectorDto)
                .toList();

        // 비디오 변환
        List<VideoDto> videos = tmdbDetail.getVideos().getResults().stream()
                .map(MediaMapper::toVideoDto)
                .toList();

        // 추가 정보들 변환
        List<ImageDto> images = toImageDtoList(tmdbDetail.getImages());
        List<MediaSummaryDto> recommendations = toMovieRecommendationsList(tmdbDetail.getRecommendations());
        List<MediaSummaryDto> similar = toMovieSimilarList(tmdbDetail.getSimilar());
        List<KeywordDto> keywords = toKeywordDtoList(tmdbDetail.getKeywords());
        WatchProvidersDto watchProviders = toWatchProvidersDto(tmdbDetail.getWatchProviders());

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
                .genres(genres)
                .cast(cast)
                .directors(directors)
                .videos(videos)
                .images(images)
                .recommendations(recommendations)
                .similar(similar)
                .keywords(keywords)
                .watchProviders(watchProviders)
                .build();
    }

    /**
     * TMDB TV 시리즈 상세 응답을 MediaDetailDto로 변환
     */
    public static MediaDetailDto toMediaDetailDto(TmdbTvSeriesDetailResponse tmdbDetail) {
        // 장르 변환
        List<GenreDto> genres = tmdbDetail.getGenres().stream()
                .map(MediaMapper::toGenreDto)
                .toList();

        // 캐스트 변환 (상위 10명만)
        List<CastDto> cast = tmdbDetail.getCredits().getCast().stream()
                .limit(10)
                .map(MediaMapper::toCastDto)
                .toList();

        // 감독 변환 (TV의 경우 Creator나 Executive Producer 등도 포함)
        List<DirectorDto> directors = tmdbDetail.getCredits().getCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob()) ||
                        "Creator".equals(crew.getJob()) ||
                        "Executive Producer".equals(crew.getJob()))
                .map(MediaMapper::toDirectorDto)
                .toList();

        // 비디오 변환
        List<VideoDto> videos = tmdbDetail.getVideos().getResults().stream()
                .map(MediaMapper::toVideoDto)
                .toList();

        // 추가 정보들 변환
        List<ImageDto> images = toImageDtoList(tmdbDetail.getImages());
        List<MediaSummaryDto> recommendations = toTvRecommendationsList(tmdbDetail.getRecommendations());
        List<MediaSummaryDto> similar = toTvSimilarList(tmdbDetail.getSimilar());
        List<KeywordDto> keywords = toKeywordDtoList(tmdbDetail.getKeywords());
        WatchProvidersDto watchProviders = toWatchProvidersDto(tmdbDetail.getWatchProviders());

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
                .genres(genres)
                .cast(cast)
                .directors(directors)
                .videos(videos)
                .images(images)
                .recommendations(recommendations)
                .similar(similar)
                .keywords(keywords)
                .watchProviders(watchProviders)
                .build();
    }

    /**
     * TMDB 영화 목록을 차트 페이지 응답으로 변환
     */
    public static PagedResponse<MediaChartDto> toMovieChartPagedResponse(
            List<TmdbMovieResponse> tmdbMovies, int page, int size) {

        List<MediaChartDto> chartDtos = IntStream.range(0, tmdbMovies.size())
                .mapToObj(i -> toMediaChartDto(tmdbMovies.get(i), i + 1 + (page * size)))
                .toList();

        return PagedResponse.<MediaChartDto>builder()
                .content(chartDtos)
                .page(page + 1)
                .size(size)
                .totalElements(chartDtos.size())
                .totalPages(calculateTotalPages(chartDtos.size(), size))
                .last(page >= calculateTotalPages(chartDtos.size(), size) - 1)
                .build();
    }

    /**
     * TMDB TV 시리즈 목록을 차트 페이지 응답으로 변환
     */
    public static PagedResponse<MediaChartDto> toTvChartPagedResponse(
            List<TmdbTvSeriesResponse> tmdbTvSeries, int page, int size) {

        List<MediaChartDto> chartDtos = IntStream.range(0, tmdbTvSeries.size())
                .mapToObj(i -> toMediaChartDto(tmdbTvSeries.get(i), i + 1 + (page * size)))
                .toList();

        return PagedResponse.<MediaChartDto>builder()
                .content(chartDtos)
                .page(page + 1)
                .size(size)
                .totalElements(chartDtos.size())
                .totalPages(calculateTotalPages(chartDtos.size(), size))
                .last(page >= calculateTotalPages(chartDtos.size(), size) - 1)
                .build();
    }

    /**
     * TMDB 멀티 검색 결과를 MediaSummaryDto 페이지 응답으로 변환
     */
    public static PagedResponse<MediaSummaryDto> toSearchPagedResponse(
            List<TmdbMultiSearchResponse> searchResults, int page, int size) {

        List<MediaSummaryDto> summaryDtos = searchResults.stream()
                .filter(result -> "movie".equals(result.getMedia_type()) || "tv".equals(result.getMedia_type()))
                .map(MediaMapper::toMediaSummaryDto)
                .toList();

        return PagedResponse.<MediaSummaryDto>builder()
                .content(summaryDtos)
                .page(page)
                .size(size)
                .totalElements(summaryDtos.size())
                .totalPages(calculateTotalPages(summaryDtos.size(), size))
                .last(page >= calculateTotalPages(summaryDtos.size(), size) - 1)
                .build();
    }

    // === 개별 변환 메서드들 ===

    public static GenreDto toGenreDto(TmdbGenreResponse tmdbGenre) {
        return GenreDto.builder()
                .id(tmdbGenre.getId())
                .name(tmdbGenre.getName())
                .build();
    }

    public static CastDto toCastDto(TmdbCastResponse tmdbCast) {
        return CastDto.builder()
                .id(tmdbCast.getId())
                .name(tmdbCast.getName())
                .character(tmdbCast.getCharacter())
                .profilePath(tmdbCast.getProfile_path())
                .build();
    }

    public static DirectorDto toDirectorDto(TmdbCrewResponse tmdbCrew) {
        return DirectorDto.builder()
                .id(tmdbCrew.getId())
                .name(tmdbCrew.getName())
                .profilePath(tmdbCrew.getProfile_path())
                .build();
    }

    public static VideoDto toVideoDto(TmdbVideoResponse tmdbVideo) {
        return VideoDto.builder()
                .name(tmdbVideo.getName())
                .key(tmdbVideo.getKey())
                .site(tmdbVideo.getSite())
                .type(tmdbVideo.getType())
                .build();
    }

    public static MediaChartDto toMediaChartDto(TmdbMovieResponse tmdbMovie, int rank) {
        return MediaChartDto.builder()
                .mediaId(tmdbMovie.getId())
                .title(tmdbMovie.getTitle())
                .posterPath(tmdbMovie.getPoster_path())
                .voteAverage(tmdbMovie.getVote_average())
                .isAnimation(isAnimation(tmdbMovie.getGenre_ids()))
                .rank(rank)
                .build();
    }

    public static MediaChartDto toMediaChartDto(TmdbTvSeriesResponse tmdbTvSeries, int rank) {
        return MediaChartDto.builder()
                .mediaId(tmdbTvSeries.getId())
                .title(tmdbTvSeries.getName())
                .posterPath(tmdbTvSeries.getPoster_path())
                .voteAverage(tmdbTvSeries.getVote_average())
                .isAnimation(isAnimation(tmdbTvSeries.getGenre_ids()))
                .rank(rank)
                .build();
    }

    public static MediaSummaryDto toMediaSummaryDto(TmdbMultiSearchResponse searchResult) {
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

    // === 복합 변환 메서드들 ===

    public static List<ImageDto> toImageDtoList(TmdbImagesResponse imagesResponse) {
        if (imagesResponse == null) return List.of();

        List<ImageDto> images = new ArrayList<>();

        // 백드롭 이미지들 추가 (최대 5개)
        if (imagesResponse.getBackdrops() != null) {
            images.addAll(imagesResponse.getBackdrops().stream()
                    .limit(5)
                    .map(MediaMapper::toImageDto)
                    .toList());
        }

        // 포스터 이미지들 추가 (최대 5개)
        if (imagesResponse.getPosters() != null) {
            images.addAll(imagesResponse.getPosters().stream()
                    .limit(5)
                    .map(MediaMapper::toImageDto)
                    .toList());
        }

        return images;
    }

    public static ImageDto toImageDto(TmdbImageResponse tmdbImage) {
        return ImageDto.builder()
                .filePath(tmdbImage.getFile_path())
                .aspectRatio(tmdbImage.getAspect_ratio())
                .height(tmdbImage.getHeight())
                .width(tmdbImage.getWidth())
                .voteAverage(tmdbImage.getVote_average())
                .voteCount(tmdbImage.getVote_count())
                .build();
    }

    public static List<MediaSummaryDto> toMovieRecommendationsList(TmdbPagedResponse<TmdbMovieResponse> recommendations) {
        if (recommendations == null || recommendations.getResults() == null) return List.of();

        return recommendations.getResults().stream()
                .limit(10) // 최대 10개만
                .map(movie -> MediaSummaryDto.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .posterPath(movie.getPoster_path())
                        .voteAverage(movie.getVote_average())
                        .isAnimation(isAnimation(movie.getGenre_ids()))
                        .build())
                .toList();
    }

    public static List<MediaSummaryDto> toTvRecommendationsList(TmdbPagedResponse<TmdbTvSeriesResponse> recommendations) {
        if (recommendations == null || recommendations.getResults() == null) return List.of();

        return recommendations.getResults().stream()
                .limit(10) // 최대 10개만
                .map(tv -> MediaSummaryDto.builder()
                        .id(tv.getId())
                        .title(tv.getName())
                        .posterPath(tv.getPoster_path())
                        .voteAverage(tv.getVote_average())
                        .isAnimation(isAnimation(tv.getGenre_ids()))
                        .build())
                .toList();
    }

    public static List<MediaSummaryDto> toMovieSimilarList(TmdbPagedResponse<TmdbMovieResponse> similar) {
        if (similar == null || similar.getResults() == null) return List.of();

        return similar.getResults().stream()
                .limit(10) // 최대 10개만
                .map(movie -> MediaSummaryDto.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .posterPath(movie.getPoster_path())
                        .voteAverage(movie.getVote_average())
                        .isAnimation(isAnimation(movie.getGenre_ids()))
                        .build())
                .toList();
    }

    public static List<MediaSummaryDto> toTvSimilarList(TmdbPagedResponse<TmdbTvSeriesResponse> similar) {
        if (similar == null || similar.getResults() == null) return List.of();

        return similar.getResults().stream()
                .limit(10) // 최대 10개만
                .map(tv -> MediaSummaryDto.builder()
                        .id(tv.getId())
                        .title(tv.getName())
                        .posterPath(tv.getPoster_path())
                        .voteAverage(tv.getVote_average())
                        .isAnimation(isAnimation(tv.getGenre_ids()))
                        .build())
                .toList();
    }

    public static List<KeywordDto> toKeywordDtoList(TmdbKeywordsResponse keywordsResponse) {
        if (keywordsResponse == null) return List.of();

        List<TmdbKeywordResponse> keywords = keywordsResponse.getKeywords() != null
                ? keywordsResponse.getKeywords()
                : keywordsResponse.getResults();

        if (keywords == null) return List.of();

        return keywords.stream()
                .map(keyword -> KeywordDto.builder()
                        .id(keyword.getId())
                        .name(keyword.getName())
                        .build())
                .toList();
    }

    public static WatchProvidersDto toWatchProvidersDto(TmdbWatchProvidersResponse watchProvidersResponse) {
        if (watchProvidersResponse == null || watchProvidersResponse.getResults() == null) {
            return WatchProvidersDto.builder().build();
        }

        // 한국(KR) 정보 우선, 없으면 미국(US) 정보 사용
        TmdbCountryWatchProvidersResponse countryProviders =
                watchProvidersResponse.getResults().get("KR");
        if (countryProviders == null) {
            countryProviders = watchProvidersResponse.getResults().get("US");
        }

        if (countryProviders == null) {
            return WatchProvidersDto.builder().build();
        }

        return WatchProvidersDto.builder()
                .link(countryProviders.getLink())
                .streaming(toProviderDtoList(countryProviders.getFlatrate()))
                .rent(toProviderDtoList(countryProviders.getRent()))
                .buy(toProviderDtoList(countryProviders.getBuy()))
                .build();
    }

    public static List<ProviderDto> toProviderDtoList(List<TmdbProviderResponse> providers) {
        if (providers == null) return List.of();

        return providers.stream()
                .map(provider -> ProviderDto.builder()
                        .providerId(provider.getProvider_id())
                        .providerName(provider.getProvider_name())
                        .logoPath(provider.getLogo_path())
                        .displayPriority(provider.getDisplay_priority())
                        .build())
                .toList();
    }

    // === 유틸리티 메서드들 ===

    public static boolean isAnimation(List<Long> genreIds) {
        // 애니메이션 장르 ID (TMDB에서 16이 애니메이션)
        return genreIds != null && genreIds.contains(16L);
    }

    public static LocalDate parseLocalDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateString, e);
            return null;
        }
    }

    public static int calculateTotalPages(int totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }
}