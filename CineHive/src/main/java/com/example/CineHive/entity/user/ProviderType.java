package com.example.CineHive.entity.user;

import com.example.CineHive.config.converter.StringValueConvertible; // [추가] import
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

/**
 * 회원의 가입 경로를 구분하기 위한 Enum 타입입니다.
 * 일반 가입(LOCAL)과 다양한 소셜 로그인 플랫폼을 정의합니다.
 */
public enum ProviderType implements StringValueConvertible {
    LOCAL("local"),
    NAVER("naver"),
    KAKAO("kakao"),
    GOOGLE("google");

    private final String value;

    ProviderType(String value) {
        this.value = value;
    }

    /**
     * [수정] 인터페이스의 요구사항을 만족시키는 메서드입니다.
     * @JsonValue 어노테이션은 그대로 유지하여 JSON 변환 시에도 이 값을 사용합니다.
     * @return 클라이언트가 사용하는 소문자 플랫폼 이름
     */
    @JsonValue
    @Override
    public String getClientValue() {
        return value;
    }

    /**
     * 문자열 값을 받아 해당하는 ProviderType Enum 상수를 찾아 반환합니다.
     * @JsonCreator 어노테이션 덕분에 JSON 역직렬화 시에도 사용됩니다.
     *
     * @param value 변환할 소문자 문자열 (예: "kakao")
     * @return 해당하는 ProviderType Enum 상수 (예: ProviderType.KAKAO)
     */
    @JsonCreator
    public static ProviderType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Stream.of(ProviderType.values())
                .filter(type -> type.value.equalsIgnoreCase(value)) // 내부 필드 'value' 직접 사용
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 플랫폼입니다: " + value));
    }
}