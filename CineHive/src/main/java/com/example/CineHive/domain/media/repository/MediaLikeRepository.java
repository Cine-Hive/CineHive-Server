package com.example.CineHive.domain.media.repository;

import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.media.entity.MediaLike;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaLikeRepository extends JpaRepository<MediaLike, Long> {
    boolean existsByUserAndMedia(User user, Media media);
    Optional<MediaLike> findByUserAndMedia(User user, Media media);
}