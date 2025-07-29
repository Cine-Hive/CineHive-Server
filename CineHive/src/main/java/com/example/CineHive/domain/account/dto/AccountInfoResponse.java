package com.example.CineHive.domain.account.dto;

import com.example.CineHive.domain.media.Genre;
import com.example.CineHive.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "내 정보 조회 응답 DTO")
@Builder
public record AccountInfoResponse(
        @Schema(description = "회원 고유 ID")
        Long id,
        @Schema(description = "이메일")
        String email,
        @Schema(description = "이름")
        String name,
        @Schema(description = "닉네임")
        String nickname,
        @Schema(description = "성별")
        String gender,
        @Schema(description = "선호 장르 목록")
        List<String> genres
) {
    public static AccountInfoResponse from(User user) {
        return AccountInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .gender(user.getGender().getDescription()) // Enum의 name() 대신 한글 설명(description) 사용
                .genres(user.getGenres().stream()
                        .map(Genre::getKoreanName)
                        .toList())
                .build();
    }
}