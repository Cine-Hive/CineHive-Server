package com.example.CineHive.exception;

public class DislikeNotFoundException extends BusinessException {
    public DislikeNotFoundException(Long memberId, Long boardId) {
        super(String.format("Dislike not found for Member %d on Board %d", memberId, boardId),
                ErrorCode.DISLIKE_NOT_FOUND);
    }
}