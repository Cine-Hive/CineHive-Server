package com.example.CineHive.dto.media;

import lombok.Builder;

@Builder
public record MediaSummaryResponse(
        Long id,
        String title,
        String posterPath,
        Double voteAverage,
        boolean isAnimation
) {}