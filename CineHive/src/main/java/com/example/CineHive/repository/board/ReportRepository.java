package com.example.CineHive.repository.board;

import com.example.CineHive.entity.user.User;
import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByUserAndBoard(User user, Board board);
}
