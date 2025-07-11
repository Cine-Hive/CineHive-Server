package com.example.CineHive.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 인터페이스
 */
public interface ErrorCode {
    String getCode();
    HttpStatus getHttpStatus();
    String getMessage();
} 