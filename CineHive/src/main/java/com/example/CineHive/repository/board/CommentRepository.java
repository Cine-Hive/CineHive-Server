package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository <Comment,Long> {
    List<Comment> findByUserAndBoard(SecurityProperties.User user, Board board);
}
