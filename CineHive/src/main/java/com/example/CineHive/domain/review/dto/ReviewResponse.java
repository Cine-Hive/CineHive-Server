package com.example.CineHive.domain.review.dto;

import com.example.CineHive.domain.review.Review;
import com.example.CineHive.domain.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "리뷰 정보 응답 DTO")
public record ReviewResponse(
        @Schema(description = "리뷰 ID", example = "1")
        Long id,

        @Schema(description = "리뷰 내용", example = "정말 감명 깊은 영화였습니다.")
        String content,

        @Schema(description = "별점", example = "4.5")
        double rating,

        @Schema(description = "리뷰 작성자 정보")
        ReviewAuthor author,

        @Schema(description = "리뷰 생성 시각 (UTC)", example = "2023-08-01T01:00:00Z")
        Instant createdAt,

        @Schema(description = "리뷰 마지막 수정 시각 (UTC)", example = "2023-08-01T02:30:00Z")
        Instant updatedAt
) {
    public static ReviewResponse of(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getContent(),
                review.getRating(),
                ReviewAuthor.from(review.getUser()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    @Schema(description = "리뷰 작성자 정보")
    public record ReviewAuthor(
            @Schema(description = "작성자 ID", example = "101")
            Long userId,
            @Schema(description = "작성자 닉네임", example = "영화광팬")
            String nickname
    ) {
        public static ReviewAuthor from(User user) {
            return new ReviewAuthor(user.getId(), user.getNickname());
        }
    }
}