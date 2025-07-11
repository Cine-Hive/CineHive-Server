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
                memberId, // [수정] 연관된 Member 객체에서 ID를 가져옵니다.
                memberNickname, // [추가] 연관된 Member 객체에서 닉네임을 가져옵니다.
                loginHistory.getFirstLoginDate(),
                loginHistory.getLastLoginDate(),
                loginHistory.getBrowser()
        );
    }
}