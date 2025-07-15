package com.example.CineHive.exception;

public class InvalidOAuthTokenException extends RuntimeException {
    public InvalidOAuthTokenException(String message) {
        super(message);
    }
}