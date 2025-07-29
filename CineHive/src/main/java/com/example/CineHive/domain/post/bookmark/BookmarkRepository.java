package com.example.CineHive.domain.post.bookmark;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<Bookmark> findByUserAndPost(User user, Post post);

    int countByPost_Id(Long postId);

    /**
     * 특정 게시글을 북마크한 모든 사용자 정보를 함께 조회합니다.
     * @param post 조회할 게시글 엔티티
     * @return User 정보가 포함된 Bookmark 엔티티 리스트
     */
    @EntityGraph(value = "Bookmark.withUser")
    List<Bookmark> findAllByPost(Post post);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}
