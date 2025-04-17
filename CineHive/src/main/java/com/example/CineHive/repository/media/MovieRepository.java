package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Movie;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MediaRepository<Movie> {
    // 추가 Movie 관련 쿼리 메서드
} 