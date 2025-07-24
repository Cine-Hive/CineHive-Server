package com.example.CineHive.repository.post;

import com.example.CineHive.entity.post.Bookmark;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 북마크(Bookmark) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 특정 사용자와 게시글에 해당하는 북마크가 존재하는지 확인합니다.
     * @param user 확인할 사용자 엔티티
     * @param post 확인할 게시글 엔티티
     * @return 북마크가 존재하면 true
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * 특정 사용자와 게시글로 북마크 정보를 조회합니다.
     * @param user 조회할 사용자 엔티티
     * @param post 조회할 게시글 엔티티
     * @return 북마크 정보를 담은 Optional 객체
     */
    Optional<Bookmark> findByUserAndPost(User user, Post post);

    /**
     * 특정 게시글에 대한 모든 북마크의 개수를 조회합니다.
     * @param postId 개수를 조회할 게시글의 ID
     * @return 해당 게시글의 북마크 개수
     */
    int countByPost_Id(Long postId);

    /**
     * 특정 사용자가 등록한 모든 북마크를 삭제합니다.
     * @param email 삭제할 북마크의 소유자 이메일
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}