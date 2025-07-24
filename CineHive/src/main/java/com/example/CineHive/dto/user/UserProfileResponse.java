package com.example.CineHive.dto.user;

import com.example.CineHive.entity.user.User;

/**
 * 사용자 프로필 조회 응답 DTO입니다.
 */
public record UserProfileResponse(
        Long id,
        String nickname,
        String userType
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getType().getDescription()
        );
    }
}