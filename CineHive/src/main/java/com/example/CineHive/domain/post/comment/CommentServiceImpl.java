package com.example.CineHive.domain.post.comment;

import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.example.CineHive.domain.post.comment.dto.CreateCommentRequest;
import com.example.CineHive.domain.post.comment.dto.UpdateCommentRequest;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentService 인터페이스의 구현체입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, CreateCommentRequest request, String userEmail) {
        Post post = findPostById(postId);
        User user = findUserByEmail(userEmail);

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toResponse(savedComment);
    }

    @Override
    public List<CommentResponse> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        List<Comment> comments = commentRepository.findByPost_Id(postId);
        return comments.stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Comment comment = findCommentById(commentId);

        verifyCommentOwnership(comment, user);
        comment.update(request.content());

        return CommentMapper.toResponse(comment);
    }



    @Override
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Comment comment = findCommentById(commentId);

        verifyCommentOwnership(comment, user);
        commentRepository.delete(comment);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void verifyCommentOwnership(Comment comment, User user) {
        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}