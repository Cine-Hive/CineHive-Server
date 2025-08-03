package com.example.CineHive.domain.media.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 미디어의 타입을 정의하는 Enum 클래스.
 * MOVIE(영화)와 TV(TV 시리즈)를 구분합니다.
 */
@Getter
@RequiredArgsConstructor
public enum MediaType {
    MOVIE("movie"),
    TV("tv");

    /**
     * API 경로 등에서 사용될 소문자 문자열 값입니다.
     */
    private final String value;

    /**
     * 문자열 값을 기반으로 해당하는 MediaType Enum을 반환합니다.
     *
     * @param value 변환할 문자열 (예: "movie", "TV")
     * @return 주어진 문자열에 해당하는 MediaType Enum
     * @throws IllegalArgumentException 지원하지 않는 타입의 문자열이 입력된 경우
     */
    public static MediaType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("미디어 타입은 null이거나 비어있을 수 없습니다.");
        }

        String normalizedValue = value.toLowerCase().trim();
        for (MediaType type : MediaType.values()) {
            if (type.getValue().equals(normalizedValue)) {
                return type;
            }
        }

        throw new IllegalArgumentException("잘못된 미디어 타입입니다: " + value + ". 지원되는 타입: movie, tv");
    }

    /**
     * 현재 타입이 MOVIE인지 확인합니다.
     *
     * @return 타입이 MOVIE이면 true, 아니면 false
     */
    public boolean isMovie() {
        return this == MOVIE;
    }

    /**
     * 현재 타입이 TV인지 확인합니다.
     *
     * @return 타입이 TV이면 true, 아니면 false
     */
    public boolean isTv() {
        return this == TV;
    }
}