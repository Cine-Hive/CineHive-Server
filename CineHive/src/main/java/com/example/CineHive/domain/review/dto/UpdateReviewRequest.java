package com.example.CineHive.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "리뷰 수정을 위한 요청 DTO")
public record UpdateReviewRequest(
        @Schema(description = "새로운 리뷰 내용 (10~5000자)", example = "다시 보니 더 좋네요.")
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(min = 10, max = 5000, message = "리뷰 내용은 10자 이상 5000자 이하로 작성해야 합니다.")
        String content,

        @Schema(description = "새로운 별점 (0.5 단위, 0.5~5.0 사이)", example = "5.0")
        @NotNull(message = "별점은 필수입니다.")
        @Pattern(regexp = "^(0\\.5|[1-4](\\.0|\\.5)?|5\\.0)$", message = "별점은 0.5 단위로 0.5에서 5.0 사이의 값이어야 합니다.")
        String rating
) {}