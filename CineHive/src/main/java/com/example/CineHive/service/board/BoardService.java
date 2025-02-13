package com.example.CineHive.service.board;

import com.example.CineHive.dto.board.BoardDto;
import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;

    public Board createBoard(BoardDto boardDto){
        User user = userRepository.findByMemEmail(boardDto.getEmail())
                .orElse(null);

            Board board = new Board();
            board.setBrdTitle(boardDto.getBrdTitle());
            board.setBrdContent(boardDto.getBrdContent());
            board.setUser(user);

            return boardRepository.save(board);
    }

    public BoardDto getBoardPostId(Long postId) {
        Optional<Board> boardOptional = boardRepository.findById(postId);
        return boardOptional.map(this::convertToDto).orElse(null);
    }
    private BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setBrdTitle(board.getBrdTitle());
        dto.setBrdContent(board.getBrdContent());
        dto.setNickname(board.getUser().getMemNickname());
        dto.setEmail(board.getUser().getMemEmail());
        dto.setBrgRedDate(board.getBrdRegDate());
        return dto;
    }
}
