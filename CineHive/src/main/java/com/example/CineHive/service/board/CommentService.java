package com.example.CineHive.service.board;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.mapper.CommentMapper;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentMapper commentMapper;

    public CommentDto addComment(Long boardId, String memEmail, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setBoard(board);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);

        // 댓글 수 증가
        board.setCommentCount(board.getCommentCount() + 1);
        boardRepository.save(board);

        return commentMapper.toDTO(savedComment);
    }

    public List<CommentDto> getCommentsByBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));
        List<Comment> comments = commentRepository.findByBoard(board);

        return comments.stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long boardId, Long commentId, String memEmail) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getMemEmail().equals(memEmail)) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);

        // 댓글 수 감소
        board.setCommentCount(board.getCommentCount() - 1);
        boardRepository.save(board);
    }


    public CommentDto updateComment(Long boardId, Long commentId, CommentDto commentDto, String memEmail) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getMemEmail().equals(memEmail)) {
            throw new RuntimeException("댓글을 수정할 권한이 없습니다.");
        }

        comment.setContent(commentDto.getContent());

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toDTO(updatedComment);
    }

}
