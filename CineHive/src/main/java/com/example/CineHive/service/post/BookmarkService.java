package com.example.CineHive.service.post;

/**
 * 게시글 북마크 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface BookmarkService {

    /**
     * 특정 게시글을 북마크에 추가합니다.
     * @param boardId 북마크할 게시글의 ID
     * @param memberEmail 북마크하는 회원의 이메일
     */
    void addBookmark(Long boardId, String memberEmail);

    /**
     * 특정 게시글의 북마크를 해제합니다.
     * @param boardId 북마크를 해제할 게시글의 ID
     * @param memberEmail 북마크를 해제하는 회원의 이메일
     */
    void removeBookmark(Long boardId, String memberEmail);

    /**
     * 특정 게시글의 북마크 개수를 조회합니다.
     * @param boardId 조회할 게시글의 ID
     * @return 북마크 개수
     */
    int getBookmarkCount(Long boardId);

    /**
     * 특정 사용자가 특정 게시글을 북마크했는지 확인합니다.
     * @param boardId 확인할 게시글의 ID
     * @param memberEmail 확인할 회원의 이메일
     * @return 북마크 여부 (true/false)
     */
    boolean isBookmarkedByUser(Long boardId, String memberEmail);
}