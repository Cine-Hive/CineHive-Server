package com.example.CineHive.repository.post;

import com.example.CineHive.entity.post.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글(Comment) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글에 달린 모든 댓글을 조회합니다.
     *
     * @param postId 댓글을 조회할 게시글의 ID
     * @return 해당 게시글의 모든 댓글 엔티티 리스트
     */
    List<Comment> findByPost_Id(Long postId);

    /**
     * 특정 사용자가 작성한 모든 댓글을 삭제합니다.
     *
     * @param email 삭제할 댓글의 작성자 이메일
     */
    void deleteAllByUser_Email(String email);
}