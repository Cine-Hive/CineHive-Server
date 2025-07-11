package com.example.CineHive.dto.member;

import com.example.CineHive.entity.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "로그인 성공 응답 DTO")
@Builder
public record LoginResponseDto(
        @Schema(description = "JWT 액세스 토큰")
        String token,
        @Schema(description = "로그인한 회원 정보")
        MemberInfo member
) {
    @Schema(description = "로그인한 회원의 상세 정보")
    @Builder
    public record MemberInfo(
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
            @Schema(description = "선호 장르")
            List<String> genres
    ) {}
}