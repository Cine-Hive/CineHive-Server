package com.example.CineHive.service.post;

import com.example.CineHive.dto.comment.CommentResponse;
import com.example.CineHive.dto.comment.CreateCommentRequest;
import com.example.CineHive.dto.comment.UpdateCommentRequest;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.Comment;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.post.CommentMapper;
import com.example.CineHive.repository.board.PostRepository;
import com.example.CineHive.repository.post.CommentRepository;
import com.example.CineHive.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CommentService 인터페이스의 구현체입니다.
 * 실제 데이터베이스와 상호작용하며 댓글 관련 비즈니스 로직을 수행합니다.
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
    public CommentResponse addComment(Long boardId, CreateCommentRequest request, String memberEmail) {
        Post post = findBoardById(boardId);
        User user = findMemberByEmail(memberEmail);

        Comment comment = Comment.builder()
                .content(request.content())
                .board(post)
                .member(user)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    public List<CommentResponse> getCommentsByBoard(Long boardId) {
        // 게시글이 존재하는지 먼저 확인
        findBoardById(boardId);
        List<Comment> comments = commentRepository.findByBoard_Id(boardId);
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Comment comment = findCommentById(commentId);

        verifyCommentOwnership(comment, user);
        comment.update(request.content());

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String memberEmail) {
        User user = findMemberByEmail(memberEmail);
        Comment comment = findCommentById(commentId);

        verifyCommentOwnership(comment, user);
        commentRepository.delete(comment);
    }

    //== Private Helper Methods ==//

    private User findMemberByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findBoardById(Long boardId) {
        return postRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
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
