package com.example.CineHive.service.board;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.Comment;
import com.example.CineHive.entity.post.Report;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.board.PostRepository;
import com.example.CineHive.repository.post.CommentRepository;
import com.example.CineHive.repository.post.ReportRepository;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void reportBoard(Long boardId, String reason, String reporterEmail) {
        User reporter = findMemberByEmail(reporterEmail);
        Post post = findBoardById(boardId);

        // 자신의 게시글은 신고할 수 없음
        if (post.getUser().equals(reporter)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndBoard(reporter, post)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .board(post)
                .reason(reason)
                .build();

        reportRepository.save(report);
        log.info("Member {} reported board {}", reporter.getId(), post.getId());
    }

    @Override
    @Transactional
    public void reportComment(Long commentId, String reason, String reporterEmail) {
        User reporter = findMemberByEmail(reporterEmail);
        Comment comment = findCommentById(commentId);

        // 자신의 댓글은 신고할 수 없음
        if (comment.getUser().equals(reporter)) {
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

    private User findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Post findBoardById(Long boardId) {
        return postRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
