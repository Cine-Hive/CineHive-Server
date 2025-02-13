package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.mapper.BoardMapper;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    public Board createBoard(BoardDto boardDto) {
        User user = userRepository.findByMemEmail(boardDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + boardDto.getEmail()));

        Board board = new Board();
        board.setBrdTitle(boardDto.getBrdTitle());
        board.setBrdContent(boardDto.getBrdContent());
        board.setUser(user);

        return boardRepository.save(board);
    }


    public BoardDto getBoardPostId(Long postId) {
        Optional<Board> boardOptional = boardRepository.findById(postId);
        return boardOptional.map(BoardMapper::convertToDto).orElse(null);
    }

    public Board updateBoard(Long id, String brdTitle, String brdContent) {
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            board.setBrdTitle(brdTitle);
            board.setBrdContent(brdContent);
            return boardRepository.save(board);
        } else {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }
    }

    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("게시글을 찾을 수 없습니다."));
        boardRepository.delete(board);
    }

}
