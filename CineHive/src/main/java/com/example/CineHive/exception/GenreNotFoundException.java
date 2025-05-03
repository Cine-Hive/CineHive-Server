package com.example.CineHive.exception;

/**
 * 장르를 찾을 수 없을 때 발생하는 예외
 */
public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(String message) {
        super(message);
    }
} 