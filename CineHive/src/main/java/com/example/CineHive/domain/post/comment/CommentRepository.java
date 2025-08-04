<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/post/comment/repository/CommentRepository.java
package com.example.CineHive.domain.post.comment.repository;

import com.example.CineHive.domain.post.comment.entity.Comment;
=======
package com.example.CineHive.domain.post.comment;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/post/comment/CommentRepository.java

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
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Comment> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByPost_Id(Long postId, Pageable pageable);

    /**
     * ID와 사용자 ID로 댓글을 삭제합니다. (소유권 검증 + 삭제 동시 처리)
     * @return 삭제된 행(row)의 수
     */
    @Modifying(clearAutomatically = true)
    int deleteByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
