package com.example.CineHive.domain.review.service;

import com.example.CineHive.domain.review.entity.Review;
import com.example.CineHive.domain.review.entity.ReviewLike;
import com.example.CineHive.domain.review.repository.ReviewLikeRepository;
import com.example.CineHive.domain.review.repository.ReviewRepository;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.service.AbstractLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ReviewLikeServiceImpl extends AbstractLikeService<Review, ReviewLike> implements ReviewLikeService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    public ReviewLikeServiceImpl(UserRepository userRepository, ReviewRepository reviewRepository, ReviewLikeRepository reviewLikeRepository) {
        super(userRepository);
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
    }

    @Override
    protected JpaRepository<Review, Long> getTargetRepository() {
        return this.reviewRepository;
    }

    @Override
    protected boolean isAlreadyLiked(User user, Review review) {
        return reviewLikeRepository.existsByUserAndReview(user, review);
    }

    @Override
    protected ReviewLike createLikeEntity(User user, Review review) {
        return ReviewLike.builder().user(user).review(review).build();
    }

    @Override
    @Transactional
    protected void saveLike(ReviewLike reviewLike) {
        reviewLikeRepository.save(reviewLike);
        if (reviewRepository.increaseLikeCount(reviewLike.getReview().getId()) == 0) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    protected void deleteLike(User user, Review review) {
        ReviewLike reviewLike = reviewLikeRepository.findByUserAndReview(user, review)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        reviewLikeRepository.delete(reviewLike);
        if (reviewRepository.decreaseLikeCount(review.getId()) == 0) {
            log.warn("좋아요 카운트 감소 실패: 리뷰를 찾을 수 없습니다. Review ID: {}", review.getId());
        }
    }
}