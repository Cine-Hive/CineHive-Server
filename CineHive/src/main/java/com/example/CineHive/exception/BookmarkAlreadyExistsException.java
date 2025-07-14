package com.example.CineHive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BookmarkAlreadyExistsException extends RuntimeException {
    public BookmarkAlreadyExistsException(Long memberId, Long boardId) {
        super("회원(ID: " + memberId + ")은(는) 이미 게시글(ID: " + boardId + ")을 북마크했습니다.");
    }
}