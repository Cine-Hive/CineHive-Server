package com.example.CineHive.exception;

public class LikeAlreadyExistsException extends BusinessException {
    public LikeAlreadyExistsException(Long memberId, Long boardId) {
        super(String.format("Member %d has already liked Board %d", memberId, boardId),
                ErrorCode.LIKE_ALREADY_EXISTS);
    }
}