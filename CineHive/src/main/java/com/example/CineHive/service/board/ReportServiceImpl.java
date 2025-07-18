package com.example.CineHive.service.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Comment;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
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

        // 자신의 게시글은 신고할 수 없음
        if (board.getMember().equals(reporter)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndBoard(reporter, board)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
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

        // 자신의 댓글은 신고할 수 없음
        if (comment.getMember().equals(reporter)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndComment(reporter, comment)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Board findBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
