package com.example.CineHive.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 수정을 위한 요청 데이터를 담는 DTO입니다.
 */
@Schema(description = "게시글 수정 요청 DTO")
public record UpdateBoardRequest(
        @Schema(description = "수정할 게시글 제목", example = "'듄 파트2' N차 관람 후기")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String brdTitle,

        @Schema(description = "수정할 게시글 내용", example = "볼 때마다 새로운 점이 보이네요.")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String brdContent
) {}