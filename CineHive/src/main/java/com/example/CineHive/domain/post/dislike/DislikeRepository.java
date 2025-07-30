package com.example.CineHive.domain.post.dislike;

import com.example.CineHive.domain.post.Post;
import com.example.CineHive.domain.user.User;
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
public interface DislikeRepository extends JpaRepository<Dislike, Long> {

    Optional<Dislike> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    int countByPost_Id(Long postId);

    @EntityGraph(attributePaths = {"user"})
    Page<Dislike> findAllByPost(Post post, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Dislike d WHERE d.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
