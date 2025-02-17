package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.CommentDto;
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

        return commentMapper.toDTO(savedComment);
    }

     public List<CommentDto> getCommentsByBoard(Long boardId){
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));
         List<Comment> comments = commentRepository.findByBoard(board);

         return comments.stream()
                 .map(commentMapper::toDTO)
                 .collect(Collectors.toList());
     }
}
