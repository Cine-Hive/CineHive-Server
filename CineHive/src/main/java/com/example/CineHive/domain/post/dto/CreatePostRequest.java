package com.example.CineHive.domain.post.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "게시글 생성 요청 DTO")
public record CreatePostRequest(
        @Schema(description = "게시글 제목", example = "새 영화 '듄'에 대한 소감")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "게시글 내용", example = "영상미와 사운드가 압도적이었습니다...")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String content
) {}