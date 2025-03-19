package com.example.CineHive.repository.videos.animation;


import com.example.CineHive.entity.videotype.Animation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimationRepository extends JpaRepository<Animation, Long> {
}
