package com.example.CineHive.domain.media.dto;

import lombok.Builder;

@Builder
public record MediaSummaryResponse(
        Long id,
        String title,
        String posterPath,
        Double voteAverage,
        boolean isAnimation
) {}