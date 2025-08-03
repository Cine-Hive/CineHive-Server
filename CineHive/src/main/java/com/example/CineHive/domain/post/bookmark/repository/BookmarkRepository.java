package com.example.CineHive.domain.post.controller.bookmark;

import com.example.CineHive.domain.post.controller.Post;
import com.example.CineHive.domain.user.controller.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 엔티티 대신 ID를 사용하여 존재 여부를 확인합니다. (성능 최적화)
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 북마크 존재 여부
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    int countByPost_Id(Long postId);

    @EntityGraph(attributePaths = {"user"})
    Page<Bookmark> findAllByPost(Post post, Pageable pageable);

    @Modifying(clearAutomatically = true)
    int deleteByUserAndPost(User user, Post post);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
