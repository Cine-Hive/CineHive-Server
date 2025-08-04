package com.example.CineHive.domain.post.like;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    int countByPost_Id(Long postId);

    @EntityGraph(attributePaths = {"user"})
    Page<Like> findAllByPost(Post post, Pageable pageable);

    /**
     * 특정 사용자와 게시글에 해당하는 '좋아요'를 삭제합니다.
     * @return 삭제된 행(row)의 수
     */
    @Modifying(clearAutomatically = true)
    int deleteByUserAndPost(User user, Post post);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
