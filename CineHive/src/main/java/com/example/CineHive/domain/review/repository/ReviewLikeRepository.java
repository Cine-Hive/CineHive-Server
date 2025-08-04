package com.example.CineHive.domain.review.repository;

import com.example.CineHive.domain.review.entity.Review;
import com.example.CineHive.domain.review.entity.ReviewLike;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    boolean existsByUserAndReview(User user, Review review);
    Optional<ReviewLike> findByUserAndReview(User user, Review review);
}