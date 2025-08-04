package com.example.CineHive.domain.post.comment.repository;

import com.example.CineHive.domain.post.comment.entity.Comment;
import com.example.CineHive.domain.post.comment.entity.CommentLike;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserAndComment(User user, Comment comment);

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    void deleteByUserAndComment(User user, Comment comment);
}