package com.example.CineHive.mapper;

import com.example.CineHive.dto.member.LoginHistoryDto;
import com.example.CineHive.entity.member.LoginHistory;
import com.example.CineHive.entity.member.Member;

public class LoginHistoryMapper {
    public static LoginHistoryDto toDto(LoginHistory loginHistory) {
        if (loginHistory == null) {
            return null;
        }

        Member member = loginHistory.getMember();
        Long memberId = (member != null) ? member.getId() : null;
        String memberNickname = (member != null) ? member.getNickname() : null;

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