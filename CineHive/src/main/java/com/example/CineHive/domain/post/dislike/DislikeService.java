<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/post/dislike/service/DislikeService.java
package com.example.CineHive.domain.post.dislike.service;
=======
package com.example.CineHive.domain.post.dislike;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/post/dislike/DislikeService.java

/**
 * 게시글 싫어요 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface DislikeService {

    /**
     * 특정 게시글에 '싫어요'를 추가합니다.
     * @param postId '싫어요'를 누를 게시글의 ID
     * @param userEmail '싫어요'를 누르는 사용자의 이메일
     */
    void addDislike(Long postId, String userEmail);

    /**
     * 특정 게시글의 '싫어요'를 취소합니다.
     * @param postId '싫어요'를 취소할 게시글의 ID
     * @param userEmail '싫어요'를 취소하는 사용자의 이메일
     */
    void removeDislike(Long postId, String userEmail);

    /**
     * 특정 게시글의 '싫어요' 개수를 조회합니다.
     * @param postId 조회할 게시글의 ID
     * @return '싫어요' 개수
     */
    int getDislikeCount(Long postId);
}