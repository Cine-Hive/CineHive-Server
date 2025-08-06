package com.example.CineHive.domain.search.dto;

import com.example.CineHive.domain.search.document.PostDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@Schema(description = "게시글 검색 결과 항목 응답 DTO")
public record PostSearchResponse(
        @Schema(description = "게시글 ID")
        Long postId,
        @Schema(description = "제목")
        String title,
        @Schema(description = "작성자 닉네임")
        String userNickname,
        @Schema(description = "작성 시각")
        Instant createdAt
) {
    public static PostSearchResponse from(PostDocument document) {
        return PostSearchResponse.builder()
                .postId(document.getId())
                .title(document.getTitle())
                .userNickname(document.getUserNickname())
                .createdAt(document.getCreatedAt())
                .build();
    }
}