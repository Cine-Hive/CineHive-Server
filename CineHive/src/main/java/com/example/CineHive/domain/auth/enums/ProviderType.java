package com.example.CineHive.domain.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 회원의 가입 경로를 구분하기 위한 Enum 타입입니다.
 * 일반 가입(LOCAL)과 다양한 소셜 로그인 플랫폼을 정의합니다.
 */
@Getter
public enum ProviderType {
    // state 검증이 필요 없는 경우 false
    KAKAO("kakao", false),
    NAVER("naver", true),
    GOOGLE("google", true),
    LOCAL("local", false);

    private final String value;
    private final boolean stateRequired;

    /**
     * ProviderType Enum 생성자입니다.
     * @param value JSON 변환 및 URL 경로에서 사용될 소문자 문자열 값
     * @param stateRequired OAuth2 인증 과정에서 state 파라미터가 필수인지 여부
     */
    ProviderType(String value, boolean stateRequired) {
        this.value = value;
        this.stateRequired = stateRequired;
    }

    /**
     * Enum을 JSON으로 직렬화할 때 사용될 소문자 값을 반환합니다.
     * @return 클라이언트가 사용하는 소문자 플랫폼 이름 (예: "kakao")
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 요청으로 들어온 문자열 값을 해당하는 ProviderType Enum 상수로 변환합니다.
     * 이 메서드는 대소문자를 구분하지 않습니다.
     * @param value 변환할 소문자 문자열 (예: "kakao")
     * @return 해당하는 ProviderType Enum 상수 (예: ProviderType.KAKAO)
     * @throws IllegalArgumentException 지원하지 않는 플랫폼 값일 경우 발생
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
