package com.example.CineHive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BookmarkNotFoundException extends RuntimeException {
    public BookmarkNotFoundException(Long memberId, Long boardId) {
        super("회원(ID: " + memberId + ")이 게시글(ID: " + boardId + ")에 대해 추가한 북마크를 찾을 수 없습니다.");
    }
}