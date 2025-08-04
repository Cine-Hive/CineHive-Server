package com.example.CineHive.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 유형(분류)을 정의하는 Enum 클래스.
 * 권한(Role)과 별개로, 비즈니스 로직을 분기 처리하기 위해 사용됩니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserType {

    GENERAL("일반 회원"),
    CRITIC("평론가"),
    PAID("유료 회원");

    private final String description;
}