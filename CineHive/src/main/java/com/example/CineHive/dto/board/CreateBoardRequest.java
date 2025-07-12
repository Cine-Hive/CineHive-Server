package com.example.CineHive.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 생성을 위한 요청 데이터를 담는 DTO입니다.
 */
@Schema(description = "게시글 생성 요청 DTO")
public record CreateBoardRequest(
        @Schema(description = "게시글 제목", example = "새로운 영화 '듄'에 대한 소감")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String brdTitle,

        @Schema(description = "게시글 내용", example = "영상미와 사운드가 압도적이었습니다...")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        String brdContent
) {
}