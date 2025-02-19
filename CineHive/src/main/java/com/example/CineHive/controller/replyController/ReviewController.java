package com.example.CineHive.controller.replyController;

import com.example.CineHive.entity.reply.Review;
import com.example.CineHive.service.reply.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 특정 영화의 모든 리뷰 조회
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Review>> getReviewsByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(reviewService.getReviewsByMovieId(movieId));
    }

    // 특정 사용자의 모든 리뷰 조회
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable String email) {
        return ResponseEntity.ok(reviewService.getReviewsByUserEmail(email));
    }

    // 리뷰 저장 (생성)
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestParam String email,
                                               @RequestParam Long movieId,
                                               @RequestParam String content) {
        System.out.println("POST /api/reviews 요청 받음 - email: " + email + ", movieId: " + movieId + ", content: " + content);

        Review review = new Review(null, email, movieId, content);
        return ResponseEntity.ok(reviewService.saveReview(review));
    }

    @DeleteMapping("/{movieId}/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long movieId, @PathVariable Long reviewId) {
        reviewService.deleteReview(movieId, reviewId);
        return ResponseEntity.noContent().build();
    }

}
