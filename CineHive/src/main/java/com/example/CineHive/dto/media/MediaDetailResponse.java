package com.example.CineHive.dto.media;

import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record MediaDetailResponse(
        Long id,
        String title,
        String originalTitle,
        String overview,
        LocalDate releaseDate,
        String posterPath,
        String backdropPath,
        Double voteAverage,
        Integer voteCount,
        Double popularity,
        boolean isAnimation,
        List<GenreOption> genres,
        List<CreditResponse> cast,
        List<CreditResponse> directors,
        List<VideoInfo> videos,
        List<ImageInfo> images,
        List<MediaSummaryResponse> recommendations,
        List<MediaSummaryResponse> similar,
        List<KeywordInfo> keywords,
        WatchProviderInfo watchProviders
) {}