package com.example.CineHive.service.reply;

import com.example.CineHive.entity.reply.Review;
import com.example.CineHive.repository.reply.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 특정 영화의 리뷰 가져오기
    public List<Review> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findByMovieId(movieId);
    }

    // 특정 사용자의 리뷰 가져오기
    public List<Review> getReviewsByUserEmail(String email) {
        return reviewRepository.findByUserEmail(email);
    }

    // 리뷰 저장
    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long movieId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 요청된 movieId와 리뷰가 연결된 movieId가 같은지 확인
        if (!review.getMovieId().equals(movieId)) {
            throw new RuntimeException("해당 영화의 리뷰가 아닙니다.");
        }

        reviewRepository.delete(review);
    }

}
