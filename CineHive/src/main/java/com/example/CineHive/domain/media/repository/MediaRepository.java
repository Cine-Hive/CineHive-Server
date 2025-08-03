package com.example.CineHive.domain.media.controller.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * TMDB ID와 미디어 타입을 사용하여 미디어를 조회합니다.
     * "Find or Create" 패턴의 핵심 조회 메서드입니다.
     */
    Optional<Media> findByTmdbIdAndMediaType(Integer tmdbId, MediaType mediaType);
}