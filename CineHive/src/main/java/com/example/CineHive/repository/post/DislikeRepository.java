package com.example.CineHive.repository.post;

import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.post.PostDislike;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 '싫어요'(PostDislike) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface DislikeRepository extends JpaRepository<PostDislike, Long> {

    /**
     * 특정 사용자와 게시글에 해당하는 '싫어요' 정보가 존재하는지 확인합니다.
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * 특정 사용자와 게시글로 '싫어요' 정보를 조회합니다.
     */
    Optional<PostDislike> findByUserAndPost(User user, Post post);

    /**
     * 특정 게시글에 대한 모든 '싫어요'의 개수를 조회합니다.
     */
    int countByPost_Id(Long postId);

    /**
     * 특정 사용자가 누른 모든 '싫어요'를 삭제합니다.
     */
    @Modifying
    @Query("DELETE FROM PostDislike d WHERE d.user.email = :email")
    void deleteAllByUserEmail(@Param("email") String email);
}