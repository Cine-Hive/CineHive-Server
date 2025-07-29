package com.example.CineHive.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 성별을 정의하는 Enum 클래스입니다.
 */
@Getter
@RequiredArgsConstructor
public enum Gender {

    MALE("남성"),
    FEMALE("여성"),
    OTHER("기타/비공개");

    private final String description;
}