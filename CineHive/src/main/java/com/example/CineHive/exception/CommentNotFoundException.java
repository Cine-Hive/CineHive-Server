package com.example.CineHive.exception;

/**
 * 댓글을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class CommentNotFoundException extends BusinessException {

    /**
     * ID를 기반으로 댓글을 찾지 못했을 경우 사용하는 생성자입니다.
     *
     * @param commentId 찾지 못한 댓글의 ID
     */
    public CommentNotFoundException(Long commentId) {
        super(String.format("해당 ID의 댓글을 찾을 수 없습니다: %d", commentId), ErrorCode.COMMENT_NOT_FOUND);
    }
}