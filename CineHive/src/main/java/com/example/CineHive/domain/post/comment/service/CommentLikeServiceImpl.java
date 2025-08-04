package com.example.CineHive.domain.post.comment.service;

import com.example.CineHive.domain.post.comment.entity.Comment;
import com.example.CineHive.domain.post.comment.entity.CommentLike;
import com.example.CineHive.domain.post.comment.repository.CommentLikeRepository;
import com.example.CineHive.domain.post.comment.repository.CommentRepository;
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
public class CommentLikeServiceImpl extends AbstractLikeService<Comment, CommentLike> implements CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public CommentLikeServiceImpl(UserRepository userRepository, CommentRepository commentRepository, CommentLikeRepository commentLikeRepository) {
        super(userRepository);
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Override
    protected JpaRepository<Comment, Long> getTargetRepository() {
        return this.commentRepository;
    }

    @Override
    protected boolean isAlreadyLiked(User user, Comment comment) {
        return commentLikeRepository.existsByUserAndComment(user, comment);
    }

    @Override
    protected CommentLike createLikeEntity(User user, Comment comment) {
        return CommentLike.builder().user(user).comment(comment).build();
    }

    @Override
    @Transactional
    protected void saveLike(CommentLike commentLike) {
        commentLikeRepository.save(commentLike);
        if (commentRepository.increaseLikeCount(commentLike.getComment().getId()) == 0) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    protected void deleteLike(User user, Comment comment) {
        CommentLike commentLike = commentLikeRepository.findByUserAndComment(user, comment)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        commentLikeRepository.delete(commentLike);
        if (commentRepository.decreaseLikeCount(comment.getId()) == 0) {
            log.warn("좋아요 카운트 감소 실패: 댓글을 찾을 수 없습니다. Comment ID: {}", comment.getId());
        }
    }
}