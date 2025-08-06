package com.example.CineHive.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "통합 검색 결과 응답 DTO")
public record SearchAllResponse(
        @Schema(description = "미디어(영화/TV) 검색 결과 (최대 5개)")
        List<MediaSearchResponse> media,

        @Schema(description = "게시글 검색 결과 (최대 5개)")
        List<PostSearchResponse> posts,

        @Schema(description = "인물 검색 결과 (최대 5개)")
        List<PersonSearchResponse> people
        // TODO: 향후 사용자, 컬렉션 검색 기능 추가 시 필드 확장
) {
}