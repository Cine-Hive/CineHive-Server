package com.example.CineHive.repository.board;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardDisLike;
import com.example.CineHive.entity.board.BoardLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisLikeRepository extends JpaRepository<BoardDisLike, Long> {
    Optional<BoardDisLike> findByUserAndBoard(User user, Board board);

    long countByBoard(Board board);

    List<BoardDisLike> findByUser(User user);

    void deleteByUser_MemEmail(String memEmail);
}
