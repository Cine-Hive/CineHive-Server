package com.example.CineHive.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT p FROM Post p JOIN p.user u WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    @EntityGraph(attributePaths = {"user"})
    List<Post> searchByKeyword(@Param("keyword") String keyword);

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
    @Query("UPDATE Post p SET p.dislikeCount = p.dislikeCount + 1 WHERE p.id = :postId")
    int increaseDislikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.dislikeCount = p.dislikeCount - 1 WHERE p.id = :postId AND p.dislikeCount > 0")
    int decreaseDislikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount + 1 WHERE p.id = :postId")
    int increaseBookmarkCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.bookmarkCount = p.bookmarkCount - 1 WHERE p.id = :postId AND p.bookmarkCount > 0")
    int decreaseBookmarkCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = :count WHERE p.id = :postId")
    int updateCommentCount(@Param("postId") Long postId, @Param("count") int count);


    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
