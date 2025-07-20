package com.example.CineHive.service.report;

/**
 * 사용자의 콘텐츠 신고 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 * 사용자는 게시글 또는 댓글을 신고할 수 있습니다.
 */
public interface ReportService {

    /**
     * 특정 게시글을 신고합니다.
     * 한 명의 사용자는 동일한 게시글을 중복으로 신고할 수 없습니다.
     *
     * @param boardId       신고할 게시글의 고유 ID
     * @param reason        신고 사유
     * @param reporterEmail 신고하는 회원의 이메일 (인증된 사용자 정보)
     * @throws IllegalStateException 이미 신고한 게시글일 경우 발생
     */
    void reportBoard(Long boardId, String reason, String reporterEmail);

    /**
     * 특정 댓글을 신고합니다.
     * 한 명의 사용자는 동일한 댓글을 중복으로 신고할 수 없습니다.
     *
     * @param commentId     신고할 댓글의 고유 ID
     * @param reason        신고 사유
     * @param reporterEmail 신고하는 회원의 이메일 (인증된 사용자 정보)
     * @throws IllegalStateException 이미 신고한 댓글일 경우 발생
     */
    void reportComment(Long commentId, String reason, String reporterEmail);
}