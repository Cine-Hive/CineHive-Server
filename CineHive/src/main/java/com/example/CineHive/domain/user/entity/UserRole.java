package com.example.CineHive.domain.user.controller.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 역할을 정의하는 Enum 클래스.
 * Spring Security에서는 역할(Role) 이름 앞에 'ROLE_' 접두사를 붙이는 규칙을 따릅니다.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자");

    // key 필드와 생성자에서 key 부분 삭제
    private final String description;
}