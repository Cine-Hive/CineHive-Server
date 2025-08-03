package com.example.CineHive.domain.report.controller.entity;

import com.example.CineHive.domain.report.dto.ReportRequest;
import com.example.CineHive.domain.post.comment.Comment;
import com.example.CineHive.domain.post.controller.Post;
import com.example.CineHive.domain.user.controller.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.comment.CommentRepository;
import com.example.CineHive.domain.post.controller.PostRepository;
import com.example.CineHive.domain.user.controller.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void reportPost(Long postId, ReportRequest request, String reporterEmail) {
        User reporter = findUserByEmail(reporterEmail);
        Post post = findPostById(postId);

        if (post.getUser().equals(reporter)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (reportRepository.existsByReporterAndPost(reporter, post)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .post(post)
                .reason(request.reason())
                .build();

        reportRepository.save(report);
        log.info("사용자 ID: {}가 게시글 ID: {}를 신고했습니다.", reporter.getId(), post.getId());
    }

    @Override
    @Transactional
    public void reportComment(Long commentId, ReportRequest request, String reporterEmail) {
        User reporter = findUserByEmail(reporterEmail);
        Comment comment = findCommentById(commentId);

        if (comment.getUser().equals(reporter)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (reportRepository.existsByReporterAndComment(reporter, comment)) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Report report = Report.builder()
                .reporter(reporter)
                .comment(comment)
                .reason(request.reason())
                .build();

        reportRepository.save(report);
        log.info("사용자 ID: {}가 댓글 ID: {}를 신고했습니다.", reporter.getId(), comment.getId());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
