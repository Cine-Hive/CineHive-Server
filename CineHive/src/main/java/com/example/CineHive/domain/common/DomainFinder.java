package com.example.CineHive.domain.common;

import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.media.MediaRepository;
import com.example.CineHive.domain.media.MediaType;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.post.comment.CommentRepository;
import com.example.CineHive.domain.review.Review;
import com.example.CineHive.domain.review.ReviewRepository;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.domain.user.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 여러 도메인에서 공통적으로 사용되는 엔티티 조회 및 검증 로직을 중앙에서 관리하는 헬퍼 클래스입니다.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomainFinder {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
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

    public Media findMediaByTmdbIdAndType(Integer tmdbId, MediaType mediaType) {
        return mediaRepository.findByTmdbIdAndMediaType(tmdbId, mediaType)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * ID로 특정 리뷰를 조회합니다.
     * @param reviewId 조회할 리뷰의 ID
     * @return 조회된 Review 엔티티
     */
    public Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
    }

    public Comment findCommentAndVerifyOwner(Long commentId, Long userId) {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return comment;
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

    public Media findMediaById(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }
}
