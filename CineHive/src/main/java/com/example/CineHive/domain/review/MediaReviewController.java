package com.example.CineHive.domain.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 특정 미디어에 대한 리뷰 생성을 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Review Controller", description = "미디어 리뷰 CRUD 및 상호작용 API")
@RestController
@RequestMapping("/api/v1/media/{mediaType}/{mediaId}/reviews")
@RequiredArgsConstructor
public class MediaReviewController {

    // TODO: private final ReviewService reviewService;

    @Operation(summary = "특정 미디어에 리뷰 작성")
    @PostMapping
    public void createReview(
            @PathVariable String mediaType,
            @PathVariable Long mediaId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. CreateReviewRequest DTO를 @RequestBody로 받음
        // TODO: 2. ReviewService.createReview(userEmail, mediaId, mediaType, request) 호출
        // TODO: 3. 성공(201 CREATED) 시 생성된 ReviewResponse 반환
    }
}