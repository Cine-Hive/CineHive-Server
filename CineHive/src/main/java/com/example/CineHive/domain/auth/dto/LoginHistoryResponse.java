package com.example.CineHive.domain.auth.controller.dto;

import com.example.CineHive.domain.auth.controller.LoginHistory;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public record LoginHistoryResponse(
        Long id,
        Long userId,
        String userNickname,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt,
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