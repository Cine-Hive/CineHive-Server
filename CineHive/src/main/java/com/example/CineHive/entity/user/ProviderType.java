package com.example.CineHive.entity.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * 회원의 가입 경로를 구분하기 위한 Enum 타입입니다.
 * 일반 가입(LOCAL)과 다양한 소셜 로그인 플랫폼을 정의합니다.
 */
public enum ProviderType {
    LOCAL("local"),
    NAVER("naver"),
    KAKAO("kakao"),
    GOOGLE("google");

    private final String value;

    ProviderType(String value) {
        this.value = value;
    }

    /**
     * JSON 변환 시 사용될 소문자 값을 반환합니다.
     * @return 클라이언트가 사용하는 소문자 플랫폼 이름
     */
    @JsonValue
    public String getValue() { // 인터페이스가 없다면 메서드명은 자유롭게 사용 가능합니다.
        return value;
    }

    /**
     * 문자열 값을 받아 해당하는 ProviderType Enum 상수를 찾아 반환합니다.
     * @param value 변환할 소문자 문자열 (예: "kakao")
     * @return 해당하는 ProviderType Enum 상수 (예: ProviderType.KAKAO)
     */
    @JsonCreator
    public static ProviderType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Stream.of(ProviderType.values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + value));
    }
}