package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 특정 사용자가 특정 게시글을 신고했는지 확인
    Optional<Report> findByReporterAndBoard(Member reporter, Board board);
    // 특정 사용자가 특정 댓글을 신고했는지 확인
    Optional<Report> findByReporterAndComment(Member reporter, Comment comment);
    // 특정 상태의 모든 신고 내역을 조회합니다.
    List<Report> findByStatus(ReportStatus status);

    boolean existsByReporterAndBoard(Member reporter, Board board);
    boolean existsByReporterAndComment(Member reporter, Comment comment);
}