package com.example.CineHive.exception;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class BoardNotFoundException extends BusinessException {

    /**
     * ID를 기반으로 게시글을 찾지 못했을 경우 사용하는 생성자입니다.
     *
     * @param boardId 찾지 못한 게시글의 ID
     */
    public BoardNotFoundException(Long boardId) {
        super(String.format("해당 ID의 게시글을 찾을 수 없습니다: %d", boardId), ErrorCode.BOARD_NOT_FOUND);
    }
}