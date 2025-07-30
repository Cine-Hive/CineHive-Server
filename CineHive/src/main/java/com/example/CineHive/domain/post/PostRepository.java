package com.example.CineHive.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Post> findById(Long id);

    /**
     * ID와 작성자 ID로 특정 게시글을 조회합니다. 소유권 검증에 사용됩니다.
     * @param id 조회할 게시글의 ID
     * @param userId 작성자의 ID
     * @return User 정보가 포함된 Optional<Post> 객체
     */
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

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    int incrementViews(@Param("postId") Long postId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
