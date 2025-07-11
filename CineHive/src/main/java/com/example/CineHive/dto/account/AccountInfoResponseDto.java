package com.example.CineHive.dto.account;

import com.example.CineHive.entity.member.Member;
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
    public static AccountInfoResponseDto from(Member member) {
        return AccountInfoResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .gender(member.getGender().name())
                .genres(member.getGenres().stream().toList())
                .build();
    }
}