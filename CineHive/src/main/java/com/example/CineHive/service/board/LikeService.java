package com.example.CineHive.service.board;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardLike;
import com.example.CineHive.repository.UserRepository;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service

public class LikeService {
    @Autowired
    private  LikeRepository likeRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private  BoardRepository boardRepository;

    @Transactional
    public boolean addLike(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));


        Optional<BoardLike> existingLike = likeRepository.findByUserAndBoard(user, board);
        if (existingLike.isPresent()) {
            return false;
        } else {
            BoardLike likes = new BoardLike();
            likes.setUser(user);
            likes.setBoard(board);
            likeRepository.save(likes);
            likeRepository.flush();
            // 좋아요 개수 갱신
            board.updateLikeCount();
            boardRepository.save(board);

            return true;
        }
    }

    @Transactional
    public boolean removeLike(String memEmail, Long boardId) {
        User user = userRepository.findByMemEmail(memEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Optional<BoardLike> existingLike = likeRepository.findByUserAndBoard(user, board);
        if (existingLike.isPresent()) {
            BoardLike likes = existingLike.get();
            likeRepository.delete(likes);

            likeRepository.flush();

            board.updateLikeCount();
            boardRepository.save(board);

            return true;
        } else {
            return false;
        }
    }
    public int getLikeCount(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        return board.getLikeCount();
    }

}
