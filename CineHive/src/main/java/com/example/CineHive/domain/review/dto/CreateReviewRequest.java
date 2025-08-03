package com.example.CineHive.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "리뷰 생성을 위한 요청 DTO")
public record CreateReviewRequest(
        @Schema(description = "리뷰 내용 (10자 이상 5000자 이하)", example = "정말 재미있어요! 배우들의 연기가 일품입니다.")
        @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
        @Size(min = 10, max = 5000, message = "리뷰 내용은 10자 이상 5000자 이하로 작성해야 합니다.")
        String content,

        @Schema(description = "별점 (0.5 단위, 0.5 ~ 5.0 사이)", example = "4.5")
        @NotNull(message = "별점을 입력해주세요.")
        @DecimalMin(value = "0.5", message = "별점은 0.5 이상이어야 합니다.")
        @DecimalMax(value = "5.0", message = "별점은 5.0 이하이어야 합니다.")
        double rating
) {
}