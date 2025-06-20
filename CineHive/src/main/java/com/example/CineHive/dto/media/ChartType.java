package com.example.CineHive.dto.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ChartType {
    // 영화
    POPULAR_MOVIES(MediaType.MOVIE),
    TOP_RATED_MOVIES(MediaType.MOVIE),
    UPCOMING_MOVIES(MediaType.MOVIE),
    NOW_PLAYING_MOVIES(MediaType.MOVIE),

    // TV 시리즈
    POPULAR_TV(MediaType.TV),
    TOP_RATED_TV(MediaType.TV),
    ON_THE_AIR_TV(MediaType.TV),
    AIRING_TODAY_TV(MediaType.TV),

    // 애니메이션 영화
    POPULAR_ANIMATION_MOVIES(MediaType.MOVIE),
    TOP_RATED_ANIMATION_MOVIES(MediaType.MOVIE),
    NOW_PLAYING_ANIMATION_MOVIES(MediaType.MOVIE),
    UPCOMING_ANIMATION_MOVIES(MediaType.MOVIE),

    // 애니메이션 TV
    POPULAR_ANIMATION_TV(MediaType.TV),
    TOP_RATED_ANIMATION_TV(MediaType.TV),
    ON_THE_AIR_ANIMATION_TV(MediaType.TV),
    UPCOMING_ANIMATION_TV(MediaType.TV); // TMDB에선 airing_today와 유사

    private final MediaType mediaType;

    public static ChartType fromString(String text) {
        return Arrays.stream(values())
                .filter(b -> b.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown chart type: " + text));
    }
}