package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndBoard(User user, Board board);
    void deleteByUserAndBoard(User user, Board board);
    int countByBoard(Board board);

    void deleteByUser_MemEmail(String memEmail);
}
