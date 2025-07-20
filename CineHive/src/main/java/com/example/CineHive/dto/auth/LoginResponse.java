package com.example.CineHive.dto.auth;

import com.example.CineHive.entity.media.Genre;
import com.example.CineHive.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = "로그인 성공 응답 DTO")
public record LoginResponse(
        @Schema(description = "JWT 액세스 토큰")
        String token,
        @Schema(description = "신규 회원 여부")
        boolean isNewMember,
        @Schema(description = "로그인한 회원 정보")
        UserInfo userInfo // 이름 변경
) {
    @Schema(description = "로그인한 회원의 상세 정보")
    public record UserInfo( // 이름 변경
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
                    user.getGender() != null ? user.getGender().getDescription() : null, // 한글 이름으로 변경
                    user.getGenres().stream()
                            .map(Genre::getKoreanName)
                            .collect(Collectors.toSet())
            );
        }
    }
}