package com.example.CineHive.domain.media.dto;

import com.example.CineHive.client.tmdb.dto.TmdbMovieResponse;
import com.example.CineHive.client.tmdb.dto.TmdbMultiSearchResponse;
import com.example.CineHive.client.tmdb.dto.TmdbTvSeriesResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record MediaSummaryResponse(
        Long id,
        String title,
        String posterPath,
        Double voteAverage,
        boolean isAnimation
) {
    public static MediaSummaryResponse from(TmdbMovieResponse movie) {
        return MediaSummaryResponse.builder()
                .id(movie.id())
                .title(movie.title())
                .posterPath(movie.posterPath())
                .voteAverage(movie.voteAverage())
                .isAnimation(isAnimationById(movie.genreIds()))
                .build();
    }

    public static MediaSummaryResponse from(TmdbTvSeriesResponse tv) {
        return MediaSummaryResponse.builder()
                .id(tv.id())
                .title(tv.name())
                .posterPath(tv.posterPath())
                .voteAverage(tv.voteAverage())
                .isAnimation(isAnimationById(tv.genreIds()))
                .build();
    }

    public static MediaSummaryResponse from(TmdbMultiSearchResponse multi) {
        return MediaSummaryResponse.builder()
                .id(multi.id())
                .title(multi.getUnifiedTitle())
                .posterPath(multi.posterPath())
                .voteAverage(multi.voteAverage())
                .isAnimation(isAnimationById(multi.genreIds()))
                .build();
    }

    private static boolean isAnimationById(List<Long> genreIds) {
        return genreIds != null && genreIds.contains(16L);
    }
}
