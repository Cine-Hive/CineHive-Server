package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.auth.dto.LoginHistoryResponse;

/**
 * 로그인 이력(LoginHistory) 엔티티를 DTO로 변환하는 유틸리티 클래스입니다.
 */
public final class LoginHistoryMapper {

    private LoginHistoryMapper() {}

    /**
     * LoginHistory 엔티티를 LoginHistoryResponse DTO로 변환합니다.
     * 실제 변환 로직은 LoginHistoryResponse의 from 정적 메서드에 위임합니다.
     *
     * @param loginHistory 변환할 LoginHistory 엔티티
     * @return 변환된 LoginHistoryResponse
     */
    public static LoginHistoryResponse toResponse(LoginHistory loginHistory) {
        return LoginHistoryResponse.from(loginHistory);
    }
}