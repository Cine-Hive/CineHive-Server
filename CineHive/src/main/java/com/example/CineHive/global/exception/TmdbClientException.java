package com.example.CineHive.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * TMDB API 클라이언트 통신 중 발생하는 모든 HTTP 오류를 대표하는 예외입니다.
 */
@Getter
public class TmdbClientException extends RuntimeException {

    private final HttpStatus httpStatus;

    public TmdbClientException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
    
    public HttpStatus getStatus() {
        return httpStatus;
    }
}