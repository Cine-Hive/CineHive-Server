package com.example.CineHive.repository.board;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.relational.core.sql.Like;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    Optional<LikeEntity> findByUserAndBoard(User user, Board board);

    long countByBoard(Board board);
}
