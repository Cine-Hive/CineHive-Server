package com.example.CineHive.service.board;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.dto.comment.CreateCommentRequest;
import com.example.CineHive.dto.comment.UpdateCommentRequest;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.exception.CommentNotFoundException;
import com.example.CineHive.exception.MemberNotFoundException;
import com.example.CineHive.mapper.CommentMapper;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.CommentRepository;
import com.example.CineHive.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long boardId, CreateCommentRequest request, String memberEmail) {
        Board board = findBoardById(boardId);
        Member member = findMemberByEmail(memberEmail);

        Comment comment = Comment.builder()
                .content(request.content())
                .board(board)
                .member(member)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    public List<CommentDto> getCommentsByBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            // boardId를 직접 전달하여 예외를 발생시킵니다.
            throw new BoardNotFoundException(boardId);
        }
        List<Comment> comments = commentRepository.findByBoard_Id(boardId);
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, UpdateCommentRequest request, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Comment comment = findCommentById(commentId);

        comment.verifyOwnership(member);
        comment.update(request.content());

        return CommentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String memberEmail) {
        Member member = findMemberByEmail(memberEmail);
        Comment comment = findCommentById(commentId);

        comment.verifyOwnership(member);
        commentRepository.delete(comment);
    }

    //== Private Helper Methods ==//

    /**
     * 이메일을 사용하여 회원을 찾습니다. 없으면 예외를 발생시킵니다.
     */
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    /**
     * ID를 사용하여 게시글을 찾습니다. 없으면 예외를 발생시킵니다.
     */
    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    /**
     * ID를 사용하여 댓글을 찾습니다. 없으면 예외를 발생시킵니다.
     */
    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }
}