package com.example.CineHive.domain.post.controller.like;

/**
 * 게시글 '좋아요' 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface LikeService {

    /**
     * 특정 게시글에 '좋아요'를 추가합니다.
     * @param postId '좋아요'를 추가할 게시글의 ID
     * @param userEmail '좋아요'를 누르는 사용자의 이메일
     */
    void addLike(Long postId, String userEmail);

    /**
     * 특정 게시글의 '좋아요'를 취소합니다.
     * @param postId '좋아요'를 취소할 게시글의 ID
     * @param userEmail '좋아요'를 취소하는 사용자의 이메일
     */
    void removeLike(Long postId, String userEmail);

    /**
     * 특정 게시글의 '좋아요' 개수를 조회합니다.
     * @param postId 조회할 게시글의 ID
     * @return '좋아요' 개수
     */
    int getLikeCount(Long postId);
}