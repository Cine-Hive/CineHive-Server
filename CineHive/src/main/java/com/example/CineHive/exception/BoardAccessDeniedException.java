package com.example.CineHive.exception;

/**
 * 게시물, 댓글 등 특정 리소스에 대한 접근 권한이 없을 때 발생하는 예외.
 */
public class BoardAccessDeniedException extends RuntimeException {

    public BoardAccessDeniedException(String message) {
        super(message);
    }

    public BoardAccessDeniedException() {
        this("해당 리소스에 대한 접근 권한이 없습니다.");
    }
}