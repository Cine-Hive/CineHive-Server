package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.*;
import com.example.CineHive.repository.board.BoardRepository;
import com.example.CineHive.repository.board.CommentRepository;
import com.example.CineHive.repository.board.ReportRepository;
import com.example.CineHive.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void reportBoard(Long boardId, String reason, String reporterEmail) {
        Member reporter = findMemberByEmail(reporterEmail);
        Board board = findBoardById(boardId);

        // 엔티티의 자체 검증 로직 호출 (Tell, Don't Ask)
        board.validateNotSelfReport(reporter);

        // 중복 신고 방지 (최적화된 existsBy... 메서드 사용)
        if (reportRepository.existsByReporterAndBoard(reporter, board)) {
            throw new ReportAlreadyExistsException();
        }

        Report report = Report.builder()
                .reporter(reporter)
                .board(board)
                .reason(reason)
                .build();

        reportRepository.save(report);
        log.info("Member {} reported board {}", reporter.getId(), board.getId());
    }

    @Override
    @Transactional
    public void reportComment(Long commentId, String reason, String reporterEmail) {
        Member reporter = findMemberByEmail(reporterEmail);
        Comment comment = findCommentById(commentId);

        // 엔티티의 자체 검증 로직 호출 (Tell, Don't Ask)
        comment.validateNotSelfReport(reporter);

        // 중복 신고 방지 (최적화된 existsBy... 메서드 사용)
        if (reportRepository.existsByReporterAndComment(reporter, comment)) {
            throw new ReportAlreadyExistsException();
        }

        Report report = Report.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(reason)
                .build();

        reportRepository.save(report);
        log.info("Member {} reported comment {}", reporter.getId(), comment.getId());
    }

    //== Private Helper Methods ==//

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }
}