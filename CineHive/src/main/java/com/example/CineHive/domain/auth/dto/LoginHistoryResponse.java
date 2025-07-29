package com.example.CineHive.domain.auth.dto;

import com.example.CineHive.domain.auth.LoginHistory;

import java.time.LocalDateTime;

/**
 * 로그인 이력 정보를 담는 응답 DTO입니다.
 */
public record LoginHistoryResponse(
        Long id,
        Long userId,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String browser
) {
    public static LoginHistoryResponse from(LoginHistory loginHistory) {
        return new LoginHistoryResponse(
                loginHistory.getId(),
                loginHistory.getUser().getId(),
                loginHistory.getUser().getNickname(),
                loginHistory.getCreatedAt(),
                loginHistory.getUpdatedAt(),
                loginHistory.getBrowser()
        );
    }
}