package com.example.CineHive.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO: UserDocument가 정의되면 from 정적 팩토리 메서드 구현 필요
@Schema(description = "사용자 검색 결과 응답 DTO")
public record UserSearchResponse(
        @Schema(description = "사용자 ID")
        Long userId,

        @Schema(description = "사용자 닉네임")
        String nickname,

        @Schema(description = "사용자 프로필 이미지 URL")
        String profileImageUrl
) {
}