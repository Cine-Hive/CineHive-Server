package com.example.CineHive.repository.board;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository <Comment,Long> {
    List<Comment> findByUserAndBoard(User user, Board board);

    List<Comment> findByBoard(Board board);

    @Query(value = "SELECT * FROM comment WHERE user_id = :memId", nativeQuery = true)
    List<Comment> findCommentsByUserId(@Param("memId") Long memId);

    void deleteByUser_MemEmail(String memEmail);

}
