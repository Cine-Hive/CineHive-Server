package com.example.CineHive.domain.account.dto;

import com.example.CineHive.global.validation.ValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "내 프로필 정보(닉네임 등) 수정 요청 DTO")
public record UpdateProfileRequest(
        @Schema(description = "새로운 닉네임 (공백/특수문자 제외 2~10자)", example = "씨네필전문가")
        @Pattern(regexp = ValidationPatterns.NICKNAME_PATTERN, message = "닉네임은 공백과 특수문자를 제외한 2~10자로 작성해야 합니다.")
        String nickname
        // TODO: 추후 bio, profileImageUrl 등 필드 추가
) {
}