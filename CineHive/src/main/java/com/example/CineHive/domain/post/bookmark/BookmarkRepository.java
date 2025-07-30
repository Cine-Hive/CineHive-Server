package com.example.CineHive.domain.post.bookmark;

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
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    int countByPost_Id(Long postId);

    @EntityGraph(attributePaths = {"user"})
    Page<Bookmark> findAllByPost(Post post, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Bookmark b WHERE b.user.email = :email")
    int deleteAllByUserEmail(@Param("email") String email);
}
