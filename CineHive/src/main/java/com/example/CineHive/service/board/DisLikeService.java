package com.example.CineHive.service.board;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardDisLike;
import com.example.CineHive.exception.BoardNotFoundException;
import com.example.CineHive.exception.UserNotFoundException;
import com.example.CineHive.repository.user.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.DisLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DisLikeService {

    @Autowired
    private DisLikeRepository disLikeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    // 싫어요 추가
    @Transactional
    public boolean addDisLike(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        Optional<BoardDisLike> existingDisLike = disLikeRepository.findByUserAndBoard(user, board);
        if (existingDisLike.isPresent()) {
            return false;
        } else {
            BoardDisLike disLike = new BoardDisLike();
            disLike.setUser(user);
            disLike.setBoard(board);
            disLikeRepository.save(disLike);

            // 싫어요 개수 갱신
            board.updateDisLikeCount();
            boardRepository.save(board);

            return true;
        }
    }

    // 싫어요 삭제
    @Transactional
    public boolean removeDisLike(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Optional<BoardDisLike> existingDisLike = disLikeRepository.findByUserAndBoard(user, board);
        if (existingDisLike.isPresent()) {
            BoardDisLike disLike = existingDisLike.get();
            disLikeRepository.delete(disLike);

            disLikeRepository.flush(); // DB에 즉시 반영

            board.updateDisLikeCount();
            boardRepository.save(board);

            return true;
        } else {
            return false;
        }
    }

    // 특정 게시글의 싫어요 개수 조회
    public int getDisLikeCount(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        return board.getDisLikeCount();
    }
}
