package com.example.CineHive.domain.user.dto;

<<<<<<< HEAD
import com.example.CineHive.domain.user.entity.User;
=======
import com.example.CineHive.domain.user.User;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리)

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