package com.example.CineHive.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 생성 요청 DTO")
public record CreateCommentRequest(
        @Schema(description = "댓글 내용")
        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        String content
) {}