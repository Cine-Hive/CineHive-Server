package com.example.CineHive.repository.videos.animation;

import com.example.CineHive.entity.videotype.RecommendationAnimation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimationRecommendationRepository extends JpaRepository<RecommendationAnimation, Long> {
}
