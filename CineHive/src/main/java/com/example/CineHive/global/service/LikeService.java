package com.example.CineHive.global.service;

/**
 * '좋아요' 기능에 대한 공통 서비스 인터페이스입니다.
 * 모든 '좋아요' 관련 서비스는 이 인터페이스를 구현합니다.
 */
public interface LikeService {

    /**
     * 특정 대상에 대해 '좋아요'를 등록합니다.
     * @param userEmail '좋아요'를 누른 사용자의 이메일
     * @param targetId '좋아요' 대상의 ID
     */
    void like(String userEmail, Long targetId);

    /**
     * 특정 대상에 대한 '좋아요'를 취소합니다.
     * @param userEmail '좋아요' 취소를 요청한 사용자의 이메일
     * @param targetId '좋아요' 대상의 ID
     */
    void unlike(String userEmail, Long targetId);
}