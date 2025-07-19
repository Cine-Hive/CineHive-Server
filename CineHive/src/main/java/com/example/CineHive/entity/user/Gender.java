package com.example.CineHive.entity.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 성별을 정의하는 Enum 클래스.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("MALE", "남성"),
    FEMALE("FEMALE", "여성"),
    OTHER("OTHER", "기타/비공개"); // 소셜 로그인 및 비공개 처리를 위한 값

    private final String key;
    private final String description;
}