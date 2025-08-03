package com.example.CineHive.domain.post.comment;

import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.common.dto.PageResponse;
import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.example.CineHive.domain.post.comment.dto.CreateCommentRequest;
import com.example.CineHive.domain.post.comment.dto.UpdateCommentRequest;
import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final DomainFinder domainFinder;

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, CreateCommentRequest request, String userEmail) {
        Post post = domainFinder.findPostById(postId);
        User user = domainFinder.findUserByEmail(userEmail);

        Comment comment = Comment.builder()
                .content(request.content())
                .post(post)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        if (postRepository.increaseCommentCount(postId) == 0) {
            log.warn("댓글 수 증가 실패: 존재하지 않는 게시글(ID: {})에 대한 동시성 문제 가능성", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        return CommentResponse.from(savedComment);
    }

    @Override
    public PageResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        domainFinder.findPostById(postId);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByPost_Id(postId, pageable);

        return new PageResponse<>(
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
        User user = domainFinder.findUserByEmail(userEmail);
        Comment comment = domainFinder.findCommentAndVerifyOwner(commentId, user.getId());

        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Comment comment = domainFinder.findCommentById(commentId);
        Long postId = comment.getPost().getId();

        int deletedRows = commentRepository.deleteByIdAndUserId(commentId, user.getId());
        if (deletedRows == 0) {
            if (commentRepository.existsById(commentId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            } else {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }

        // 댓글 수 원자적 감소
        if (postRepository.decreaseCommentCount(postId) == 0) {
            log.warn("댓글 수 감소 실패: 존재하지 않는 게시글(ID: {})에 대한 동시성 문제 가능성", postId);
        }
    }
}
