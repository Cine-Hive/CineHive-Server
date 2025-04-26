package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Animation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimationRepository extends MediaRepository<Animation> {
    // 애니메이션 타입으로 검색
    List<Animation> findByAnimationType(Animation.AnimationType type);
} 