package com.example.CineHive.entity.member;

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
     * Enum을 JSON으로 변환할 때 사용할 값을 반환합니다. (소문자)
     * @return 소문자 플랫폼 이름
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 문자열 값을 받아 해당하는 ProviderType Enum 상수를 찾아 반환합니다.
     * 이 메서드 덕분에 Spring이 경로 변수나 요청 파라미터의 소문자 문자열을
     * 올바르게 Enum으로 변환할 수 있습니다.
     *
     * @param value 변환할 소문자 문자열 (예: "kakao")
     * @return 해당하는 ProviderType Enum 상수 (예: ProviderType.KAKAO)
     */
    @JsonCreator // Spring이 JSON이나 요청 파라미터를 역직렬화할 때 이 메서드를 사용하도록 지정
    public static ProviderType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        // 들어온 소문자 값과 일치하는 Enum을 찾아서 반환
        return Stream.of(ProviderType.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + value));
    }
}