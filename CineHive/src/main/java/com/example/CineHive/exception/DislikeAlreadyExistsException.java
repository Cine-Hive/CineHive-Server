package com.example.CineHive.exception;

public class DislikeAlreadyExistsException extends BusinessException {
    public DislikeAlreadyExistsException(Long memberId, Long boardId) {
        super(String.format("Member %d has already disliked Board %d", memberId, boardId),
                ErrorCode.DISLIKE_ALREADY_EXISTS);
    }
}