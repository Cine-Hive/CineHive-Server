package com.example.CineHive.domain.review.dto;

import com.example.CineHive.domain.media.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "리뷰 생성을 위한 요청 DTO")
public record CreateReviewRequest(
        @Schema(description = "리뷰를 작성할 미디어의 TMDB ID", example = "550")
        @NotNull(message = "미디어 ID는 필수입니다.")
        Integer tmdbId,

        @Schema(description = "미디어 타입", example = "MOVIE")
        @NotNull(message = "미디어 타입은 필수입니다.")
        MediaType mediaType,

        @Schema(description = "리뷰 내용 (10~5000자)", example = "정말 감명 깊은 영화였습니다.")
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        @Size(min = 10, max = 5000, message = "리뷰 내용은 10자 이상 5000자 이하로 작성해야 합니다.")
        String content,

        @Schema(description = "별점 (0.5 단위, 0.5~5.0 사이)", example = "4.5")
        @NotNull(message = "별점은 필수입니다.")
        @Pattern(regexp = "^(0\\.5|[1-4](\\.0|\\.5)?|5\\.0)$", message = "별점은 0.5 단위로 0.5에서 5.0 사이의 값이어야 합니다.")
        String rating
) {}