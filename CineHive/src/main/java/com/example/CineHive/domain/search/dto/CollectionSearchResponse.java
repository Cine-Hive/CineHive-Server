package com.example.CineHive.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// TODO: CollectionDocument가 정의되면 from 정적 팩토리 메서드 구현 필요
@Schema(description = "컬렉션 검색 결과 응답 DTO")
public record CollectionSearchResponse(
        @Schema(description = "컬렉션 ID")
        Long collectionId,

        @Schema(description = "컬렉션 이름")
        String name,

        @Schema(description = "컬렉션 포스터 이미지 경로")
        String posterPath
) {
}