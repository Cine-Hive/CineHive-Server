package com.example.CineHive.domain.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "미디어 참여 인물(크레딧) 정보 응답")
@Builder
public record CreditResponse(
        @Schema(description = "인물 고유 ID")
        Long personId,

        @Schema(description = "인물 이름")
        String name,

        @Schema(description = "담당 역할", example = "Director or Actor")
        String job,

        @Schema(description = "배역 이름 (배우일 경우)")
        String character,

        @Schema(description = "프로필 이미지 경로")
        String profilePath
) {}