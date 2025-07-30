package com.example.CineHive.domain.post.comment;

import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.example.CineHive.domain.post.comment.dto.CreateCommentRequest;
import com.example.CineHive.domain.post.comment.dto.UpdateCommentRequest;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.common.dto.PagedResponse;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.PostRepository;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return CommentResponse.from(savedComment);
    }

    @Override
    public PagedResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        Post post = findPostById(postId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPost_Id(postId, pageable);

        return new PagedResponse<>(
                commentPage.getContent().stream().map(CommentResponse::from).toList(),
                commentPage.getNumber() + 1,
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Comment comment = findCommentAndVerifyOwner(commentId, user.getId());

        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Comment comment = findCommentAndVerifyOwner(commentId, user.getId());

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

    private Comment findCommentAndVerifyOwner(Long commentId, Long userId) {
        return commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> {
                    if (commentRepository.existsById(commentId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
                    } else {
                        throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
                    }
                });
    }
}
