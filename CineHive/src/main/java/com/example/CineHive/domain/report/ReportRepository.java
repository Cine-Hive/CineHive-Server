package com.example.CineHive.domain.report;

import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
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
     * @param reporter 신고자
     * @param post     신고 대상 게시글
     * @return 신고 내역이 존재하면 true
     */
    boolean existsByReporterAndPost(User reporter, Post post);

    /**
     * 특정 사용자가 특정 댓글을 신고했는지 확인합니다.
     *
     * @param reporter 신고자
     * @param comment  신고 대상 댓글
     * @return 신고 내역이 존재하면 true
     */
    boolean existsByReporterAndComment(User reporter, Comment comment);

    /**
     * 특정 상태의 모든 신고 내역을 조회합니다.
     *
     * @param status 조회할 신고 처리 상태
     * @return 해당 상태의 모든 신고 엔티티 리스트
     */
    @EntityGraph(value = "Report.withAll")
    List<Report> findByStatus(ReportStatus status);
}
