package com.example.CineHive.exception;

public class BookmarkNotFoundException extends BusinessException {

    public BookmarkNotFoundException(Long memberId, Long boardId) {
        super(String.format("Bookmark not found for Member %d on Board %d", memberId, boardId),
                ErrorCode.BOOKMARK_NOT_FOUND);
    }
}