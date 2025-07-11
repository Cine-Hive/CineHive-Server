package com.example.CineHive.exception.media;

import com.example.CineHive.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 미디어 도메인 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum MediaErrorCode implements ErrorCode {
    MEDIA_NOT_FOUND("MEDIA_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 미디어를 찾을 수 없습니다"),
    MEDIA_ALREADY_EXISTS("MEDIA_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 존재하는 미디어입니다"),
    INVALID_MEDIA_TYPE("INVALID_MEDIA_TYPE", HttpStatus.BAD_REQUEST, "유효하지 않은 미디어 타입입니다"),
    GENRE_NOT_FOUND("GENRE_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 장르를 찾을 수 없습니다"),
    MEDIA_SAVE_FAILED("MEDIA_SAVE_FAILED", HttpStatus.INTERNAL_SERVER_ERROR, "미디어 저장에 실패했습니다");
    
    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
} 