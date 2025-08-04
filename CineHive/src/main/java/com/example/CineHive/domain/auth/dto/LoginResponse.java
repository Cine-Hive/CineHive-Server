package com.example.CineHive.domain.auth.dto;

import com.example.CineHive.domain.media.Genre;
import com.example.CineHive.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "로그인 성공 응답 DTO")
public record LoginResponse(
        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJI...")
        String accessToken,

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJI...")
        String refreshToken,

        @Schema(description = "신규 회원 여부")
        boolean isNewUser,

        @Schema(description = "로그인한 회원 정보")
        UserInfo userInfo
) {
    @Schema(description = "로그인한 회원의 상세 정보")
    public record UserInfo(
            Long id,
            String email,
            String name,
            String nickname,
            String gender,
            Set<String> genres
    ) {
        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getNickname(),
                    user.getGender() != null ? user.getGender().getDescription() : null,
                    user.getGenres().stream()
                            .map(Genre::getKoreanName)
                            .collect(Collectors.toSet())
            );
        }
    }
}
