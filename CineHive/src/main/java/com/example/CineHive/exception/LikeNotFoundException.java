package com.example.CineHive.exception;

public class LikeNotFoundException extends BusinessException {
    public LikeNotFoundException(Long memberId, Long boardId) {
        super(String.format("Like not found for Member %d on Board %d", memberId, boardId),
                ErrorCode.LIKE_NOT_FOUND);
    }
}