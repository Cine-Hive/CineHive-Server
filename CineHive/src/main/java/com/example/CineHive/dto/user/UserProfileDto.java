package com.example.CineHive.dto.user;

import com.example.CineHive.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 프로필 조회 응답 DTO")
public record UserProfileDto(
        Long id,
        String nickname,
        String userType
        // 필요에 따라 팔로워 수, 프로필 이미지 등 추가
) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getNickname(),
                user.getType().getDescription()
        );
    }
}