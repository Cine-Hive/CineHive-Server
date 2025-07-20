package com.example.CineHive.mapper;

import com.example.CineHive.dto.user.LoginHistoryResponse;
import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;

public class LoginHistoryMapper {
    public static LoginHistoryResponse toDto(LoginHistory loginHistory) {
        if (loginHistory == null) {
            return null;
        }

        User user = loginHistory.getUser();
        Long memberId = (user != null) ? user.getId() : null;
        String memberNickname = (user != null) ? user.getNickname() : null;

        return new LoginHistoryResponse(
                loginHistory.getId(),
                memberId,
                memberNickname,
                loginHistory.getFirstLoginDate(),
                loginHistory.getLastLoginDate(),
                loginHistory.getBrowser()
        );
    }
}