package com.example.CineHive.domain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "내 선호 설정(장르 등) 수정 요청 DTO")
public record UpdatePreferencesRequest(
        @Schema(description = "새로운 선호 장르 목록 (장르 Enum 이름과 일치해야 함)")
        @NotNull
        @Size(min = 1, message = "선호 장르는 최소 1개 이상 선택해야 합니다.")
        Set<String> genres
        // TODO: 추후 알림 설정 등 필드 추가
) {
}