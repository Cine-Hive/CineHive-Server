package com.example.CineHive.domain.media.controller.dto;

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

    /**
     * 문자열을 해당하는 Platform Enum으로 변환합니다.
     * @param text 변환할 Enum 상수 이름 (대소문자 무시)
     * @return 변환된 Platform
     * @throws IllegalArgumentException 지원하지 않는 플랫폼일 경우
     */
    public static Platform from(String text) { // fromString -> from
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + text));
    }

    /**
     * TMDB 네트워크 ID로 해당하는 Platform Enum을 변환합니다.
     * @param id 찾으려는 플랫폼의 TMDB ID
     * @return ID에 해당하는 Platform Enum
     * @throws IllegalArgumentException 지원하지 않는 ID일 경우
     */
    public static Platform fromId(Long id) {
        return Arrays.stream(values())
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼 ID입니다: " + id));
    }
}