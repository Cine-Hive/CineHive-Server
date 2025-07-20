package com.example.CineHive.dto.account;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.media.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "내 정보 조회 응답 DTO")
@Builder
public record AccountInfoResponseDto(
        Long id,
        String email,
        String name,
        String nickname,
        String gender,
        List<String> genres
) {
    public static AccountInfoResponseDto from(User user) {
        return AccountInfoResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .gender(user.getGender().name())
                .genres(user.getGenres().stream()
                        .map(Genre::getKoreanName)
                        .toList())
                .build();
    }
}