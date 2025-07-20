package com.example.CineHive.service.post;

/**
 * 게시글 '좋아요' 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface LikeService {

    /**
     * 특정 게시글에 '좋아요'를 추가합니다.
     * @param boardId '좋아요'를 추가할 게시글의 ID
     * @param memberEmail '좋아요'를 누르는 회원의 이메일
     */
    void addLike(Long boardId, String memberEmail);

    /**
     * 특정 게시글의 '좋아요'를 취소합니다.
     * @param boardId '좋아요'를 취소할 게시글의 ID
     * @param memberEmail '좋아요'를 취소하는 회원의 이메일
     */
    void removeLike(Long boardId, String memberEmail);

    /**
     * 특정 게시글의 '좋아요' 개수를 조회합니다.
     * @param boardId 조회할 게시글의 ID
     * @return '좋아요' 개수
     */
    int getLikeCount(Long boardId);
}