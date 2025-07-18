package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 신고(Report) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 특정 사용자가 특정 게시글을 신고했는지 확인합니다.
     *
     * @param reporter 신고자 회원 엔티티
     * @param board    신고 대상 게시글 엔티티
     * @return 신고 내역이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByReporterAndBoard(Member reporter, Board board);

    /**
     * 특정 사용자가 특정 댓글을 신고했는지 확인합니다.
     *
     * @param reporter 신고자 회원 엔티티
     * @param comment  신고 대상 댓글 엔티티
     * @return 신고 내역이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByReporterAndComment(Member reporter, Comment comment);

    /**
     * 특정 상태(예: PENDING, ACCEPTED)의 모든 신고 내역을 조회합니다.
     * 관리자가 신고 내역을 필터링하여 볼 때 사용됩니다.
     *
     * @param status 조회할 신고 처리 상태
     * @return 해당 상태의 모든 신고 엔티티 리스트
     */
    List<Report> findByStatus(ReportStatus status);
}
