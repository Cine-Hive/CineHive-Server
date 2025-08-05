package com.example.CineHive.domain.post.repository;

import com.example.CineHive.domain.post.entity.Post;
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
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Post> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Optional<Post> findByIdAndUserId(Long id, Long userId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Post> findAll(Pageable pageable);

    @Query(value = "SELECT p FROM Post p JOIN p.user u WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))",
            countQuery = "SELECT COUNT(p) FROM Post p JOIN p.user u WHERE " +
                    "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    @EntityGraph(attributePaths = {"user"})
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    int incrementViews(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    int increaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    int decreaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount + 1 WHERE p.id = :postId")
    int increaseBookmarkCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount - 1 WHERE p.id = :postId AND p.bookmarkCount > 0")
    int decreaseBookmarkCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    int increaseCommentCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    int decreaseCommentCount(@Param("postId") Long postId);

    /**
     * ID와 사용자 ID로 게시글을 삭제합니다. (소유권 검증 + 삭제 동시 처리)
     * @return 삭제된 행(row)의 수
     */
    @Modifying(clearAutomatically = true)
    int deleteByIdAndUserId(Long id, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
