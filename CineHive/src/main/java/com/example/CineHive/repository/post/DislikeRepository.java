package com.example.CineHive.repository.post;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.PostDislike;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DislikeRepository extends JpaRepository<PostDislike, Long> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<PostDislike> findByUserAndPost(User user, Post post);

    int countByPost_Id(Long postId);

    /**
     * 특정 게시글에 '싫어요'를 누른 모든 사용자 정보를 함께 조회합니다.
     * @param post 조회할 게시글 엔티티
     * @return User 정보가 포함된 PostDislike 엔티티 리스트
     */
    @EntityGraph(value = "PostDislike.withUser")
    List<PostDislike> findAllByPost(Post post);

    @Modifying
    @Query("DELETE FROM PostDislike d WHERE d.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}
