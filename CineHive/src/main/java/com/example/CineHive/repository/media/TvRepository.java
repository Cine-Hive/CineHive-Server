package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Tv;
import org.springframework.stereotype.Repository;

@Repository
public interface TvRepository extends MediaRepository<Tv> {
    // 추가 TV 관련 쿼리 메서드
} 