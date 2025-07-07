package com.example.CineHive.dto.media;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Platform {
    // --- 해외 주요 플랫폼 ---
    NETFLIX(213L, "Netflix"),
    DISNEY_PLUS(2739L, "Disney+"),
    AMAZON_PRIME(1024L, "Amazon Prime Video"),
    HBO(49L, "HBO"),
    APPLE_TV_PLUS(2552L, "Apple TV+"),
    HULU(453L, "Hulu"),
    PARAMOUNT_PLUS(4330L, "Paramount+"),
    PEACOCK(3353L, "Peacock"),
    YOUTUBE_PREMIUM(1436L, "YouTube Premium"),

    // --- 국내 주요 OTT ---
    WAVVE(3321L, "Wavve"),
    TVING(3320L, "TVING"),
    WATCHA(3522L, "Watcha"),
    COUPANG_PLAY(3588L, "Coupang Play"),
    LAFTEL(3636L, "Laftel"),
    NAVER_SERIES_ON(3791L, "Naver Series On"),
    UPLUS_MOBILE_TV(3730L, "U+ Mobile TV"),
    KAKAO_TV(3471L, "Kakao TV"),

    // --- 국내 방송사 ---
    TVN(318L, "tvN"),
    JTBC(269L, "JTBC"),
    SBS(67L, "SBS"),
    KBS(62L, "KBS"),
    MBC(74L, "MBC");

    private final Long id;
    private final String displayName;

    public static Platform fromString(String text) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown platform: " + text));
    }
}