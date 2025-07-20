package com.example.CineHive.dto.user;

import com.example.CineHive.entity.user.LoginHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "로그인 이력 응답 DTO")
public record LoginHistoryDto(
        Long id,
        Long userId,
        String userNickname,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String browser
) {
    public static LoginHistoryDto from(LoginHistory loginHistory) {
        return new LoginHistoryDto(
                loginHistory.getId(),
                loginHistory.getUser().getId(),
                loginHistory.getUser().getNickname(),
                loginHistory.getCreatedAt(),
                loginHistory.getUpdatedAt(),
                loginHistory.getBrowser()
        );
    }
}