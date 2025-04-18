package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Tv;
import org.springframework.stereotype.Repository;

@Repository
public interface TvRepository extends MediaRepository<Tv> {
    // 추가 메서드가 필요하면 여기에 정의
} 