package com.example.CineHive.mapper;

import com.example.CineHive.dto.user.LoginHistoryDto;
import com.example.CineHive.entity.LoginHistory;

public class LoginHistoryMapper {
    public static LoginHistoryDto toDto(LoginHistory loginHistory) {
        return new LoginHistoryDto(
                loginHistory.getId(),
                loginHistory.getUser().getMemEmail(),
                loginHistory.getFirstLoginDate(),
                loginHistory.getLastLoginDate(),
                loginHistory.getBrowser()
        );
    }
}
