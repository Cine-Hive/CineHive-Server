package com.example.CineHive.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 수정 요청 DTO")
public record UpdateCommentRequest(
        @Schema(description = "수정할 댓글 내용")
        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        String content
) {}