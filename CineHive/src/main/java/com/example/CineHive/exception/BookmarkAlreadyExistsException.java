package com.example.CineHive.exception;

public class BookmarkAlreadyExistsException extends BusinessException {

    public BookmarkAlreadyExistsException(Long memberId, Long boardId) {
        super(String.format("Member %d has already bookmarked Board %d", memberId, boardId),
                ErrorCode.BOOKMARK_ALREADY_EXISTS);
    }
}