package com.example.CineHive.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * API 에러 코드를 정의하는 열거형입니다.
 * 각 에러 코드는 고유 코드, HTTP 상태 코드, 클라이언트 메시지를 포함합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common & Global (C0xx)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),
    MAPPING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "데이터 매핑 중 오류가 발생했습니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "C004", "데이터 무결성 제약 조건에 위배되었습니다."),

    // User & Auth (U0xx)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "U003", "이미 사용 중인 닉네임입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "U004", "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "U005", "인증되지 않은 접근입니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "U006", "접근 권한이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "U007", "토큰이 만료되었습니다."),

    // OAuth (O0xx)
    INVALID_OAUTH_TOKEN(HttpStatus.BAD_REQUEST, "O001", "유효하지 않은 OAuth 토큰입니다."),
    OAUTH_COMMUNICATION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "O002", "OAuth 서비스와 통신 중 오류가 발생했습니다."),

    // Banner (B0xx)
    BANNER_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "해당 배너를 찾을 수 없습니다."),

    // Post & Comment (P0xx)
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "해당 댓글을 찾을 수 없습니다."),

    // Like, Dislike, Bookmark (I0xx - Interaction)
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "I001", "이미 '좋아요'를 누른 게시글입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "'좋아요'를 누르지 않은 게시글입니다."),
    DISLIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "I003", "이미 '싫어요'를 누른 게시글입니다."),
    DISLIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "I004", "'싫어요'를 누르지 않은 게시글입니다."),
    BOOKMARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "I005", "이미 북마크한 게시글입니다."),
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "I006", "북마크하지 않은 게시글입니다."),

    // Report (R0xx)
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "신고 정보를 찾을 수 없습니다."),
    REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "R002", "이미 신고한 콘텐츠입니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "R003", "자신이 작성한 콘텐츠는 신고할 수 없습니다."),
    REPORT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "R004", "이미 처리된 신고입니다."),

    // Media & External API (M0xx)
    INVALID_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "M001", "유효하지 않은 미디어 타입입니다."),
    CHART_STRATEGY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "M002", "해당 차트 타입을 처리할 수 없습니다."),
    GENRE_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "해당 장르를 찾을 수 없습니다."),
    TMDB_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "M004", "외부 영화 API 호출 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}