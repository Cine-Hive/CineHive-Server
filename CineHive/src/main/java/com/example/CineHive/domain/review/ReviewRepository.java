package com.example.CineHive.domain.review;

import com.example.CineHive.domain.media.Media;
import com.example.CineHive.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 사용자와 미디어를 기준으로 리뷰가 이미 존재하는지 확인합니다.
     */
    boolean existsByUserAndMedia(User user, Media media);

    /**
     * 특정 미디어에 달린 모든 리뷰의 별점 총합을 계산합니다.
     * (평균 별점 계산 시 사용)
     */
    @Query("SELECT SUM(r.rating) FROM Review r WHERE r.media = :media")
    Optional<Double> sumRatingByMedia(@Param("media") Media media);

    /**
     * 특정 미디어에 달린 전체 리뷰 수를 계산합니다.
     */
    long countByMedia(Media media);

    /**
     * 특정 미디어에 대한 리뷰 목록을 페이징하여 조회합니다.
     */
    Page<Review> findByMedia(Media media, Pageable pageable);
}