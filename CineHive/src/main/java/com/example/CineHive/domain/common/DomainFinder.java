package com.example.CineHive.domain.common;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.post.comment.CommentRepository;
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

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * 게시글을 조회하고 소유권을 검증합니다.
     * @param postId  조회 및 검증할 게시글 ID
     * @param userId  검증할 사용자 ID
     * @return 검증된 Post 엔티티
     */
    public Post findPostAndVerifyOwner(Long postId, Long userId) {
        Post post = findPostById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return post;
    }

    /**
     * 댓글을 조회하고 소유권을 검증합니다.
     * @param commentId 조회 및 검증할 댓글 ID
     * @param userId    검증할 사용자 ID
     * @return 검증된 Comment 엔티티
     */
    public Comment findCommentAndVerifyOwner(Long commentId, Long userId) {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return comment;
    }
}
