package com.example.CineHive.domain.search.dto;

import com.example.CineHive.domain.search.document.PersonDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "인물 검색 결과 항목 응답 DTO")
public record PersonSearchResponse(
        @Schema(description = "TMDB 인물 ID")
        Long tmdbId,
        @Schema(description = "이름")
        String name,
        @Schema(description = "프로필 이미지 경로")
        String profilePath
) {
    public static PersonSearchResponse from(PersonDocument document) {
        return PersonSearchResponse.builder()
                .tmdbId(document.getId())
                .name(document.getName())
                .profilePath(document.getProfilePath())
                .build();
    }
}