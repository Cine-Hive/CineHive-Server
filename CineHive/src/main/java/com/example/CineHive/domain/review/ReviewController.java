package com.example.CineHive.domain.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 개별 리뷰 수정, 삭제 및 상호작용을 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Review Controller", description = "미디어 리뷰 CRUD 및 상호작용 API")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    // TODO: private final ReviewService reviewService;
    // TODO: private final ReportService reportService; // 신고 기능

    @Operation(summary = "리뷰 수정")
    @PutMapping("/{reviewId}")
    public void updateReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. UpdateReviewRequest DTO를 @RequestBody로 받음
        // TODO: 2. ReviewService.updateReview(userEmail, reviewId, request) 호출
        // TODO: 3. 성공 시 수정된 ReviewResponse 반환
    }

    @Operation(summary = "리뷰 삭제")
    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReviewService.deleteReview(userEmail, reviewId) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "리뷰 좋아요")
    @PostMapping("/{reviewId}/like")
    public void likeReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReviewService/LikeService에서 likeReview(userEmail, reviewId) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "리뷰 좋아요 취소")
    @DeleteMapping("/{reviewId}/like")
    public void unlikeReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReviewService/LikeService에서 unlikeReview(userEmail, reviewId) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "리뷰 신고")
    @PostMapping("/{reviewId}/reports")
    public void reportReview(
            @PathVariable Long reviewId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReportRequest DTO를 @RequestBody로 받음
        // TODO: 2. ReportService.reportReview(userEmail, reviewId, request) 호출
        // TODO: 3. 성공(201 CREATED) 시 MessageResponse 반환
    }
}