package com.example.CineHive.domain.common;

import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.media.MediaRepository;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.review.Review;
import com.example.CineHive.domain.review.ReviewRepository;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainFinder {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final ReviewRepository reviewRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Media findMediaByTmdbIdAndType(Integer tmdbId, com.example.CineHive.domain.media.MediaType mediaType) {
        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }

    public Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    /**
     * 리뷰를 조회하고 소유권을 검증합니다.
     * @param reviewId 조회 및 검증할 리뷰 ID
     * @param userId   검증할 사용자 ID
     * @return 검증된 Review 엔티티
     */
    public Review findReviewAndVerifyOwner(Long reviewId, Long userId) {
        Review review = findReviewById(reviewId);
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return review;
    }
}
