package com.example.CineHive.mapper;

import com.example.CineHive.dto.member.LoginHistoryDto;
import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;

public class LoginHistoryMapper {
    public static LoginHistoryDto toDto(LoginHistory loginHistory) {
        if (loginHistory == null) {
            return null;
        }

        User user = loginHistory.getUser();
        Long memberId = (user != null) ? user.getId() : null;
        String memberNickname = (user != null) ? user.getNickname() : null;

        return new LoginHistoryDto(
                loginHistory.getId(),
                memberId,
                memberNickname,
                loginHistory.getFirstLoginDate(),
                loginHistory.getLastLoginDate(),
                loginHistory.getBrowser()
        );
    }
}