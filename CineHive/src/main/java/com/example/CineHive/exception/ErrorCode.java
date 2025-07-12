package com.example.CineHive.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API 에러 코드를 정의하는 열거형입니다.
 * 각 에러 코드는 HTTP 상태 코드와 기본 에러 메시지를 포함합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),

    // Board
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 북마크한 게시글입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "북마크하지 않은 게시글입니다."),
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 '좋아요'를 누른 게시글입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "'좋아요'를 누르지 않은 게시글입니다."),
    DISLIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 '싫어요'를 누른 게시글입니다."), // 추가
    DISLIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "'싫어요'를 누르지 않은 게시글입니다."),

    // Auth
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않은 접근입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // Report
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신이 작성한 콘텐츠는 신고할 수 없습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 신고한 콘텐츠입니다.");




    private final HttpStatus status;
    private final String message;
}