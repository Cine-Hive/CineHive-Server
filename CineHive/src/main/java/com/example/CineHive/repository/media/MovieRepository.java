package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Movie;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends MediaRepository<Movie> {
    // 추가 메서드가 필요하면 여기에 정의
} 