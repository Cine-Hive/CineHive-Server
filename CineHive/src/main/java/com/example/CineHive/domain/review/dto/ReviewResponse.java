package com.example.CineHive.domain.review.dto;

import com.example.CineHive.domain.review.Review;
import com.example.CineHive.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Schema(description = "리뷰 정보 응답 DTO")
@Builder
public record ReviewResponse(
        @Schema(description = "리뷰 ID")
        Long id,
        @Schema(description = "리뷰 내용")
        String content,
        @Schema(description = "별점")
        double rating,
        @Schema(description = "리뷰 작성자 정보")
        ReviewAuthor author,
        @Schema(description = "리뷰 생성 시각")
        LocalDateTime createdAt,
        @Schema(description = "리뷰 마지막 수정 시각")
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getContent(),
                review.getRating(),
                ReviewAuthor.from(review.getUser()),
                review.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime(),
                review.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        );
    }

    @Schema(description = "리뷰 작성자 정보")
    @Builder
    public record ReviewAuthor(
            @Schema(description = "작성자 ID")
            Long userId,
            @Schema(description = "작성자 닉네임")
            String nickname
    ) {
        public static ReviewAuthor from(User user) {
            return new ReviewAuthor(user.getId(), user.getNickname());
        }
    }
}