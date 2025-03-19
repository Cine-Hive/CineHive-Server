package com.example.CineHive.repository.videos.animation;

import com.example.CineHive.entity.credit.animation.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimationDirectorRepository extends JpaRepository<Director,Long> {
    Director findByName(String name);
}
