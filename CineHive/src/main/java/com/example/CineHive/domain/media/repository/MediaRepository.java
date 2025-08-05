package com.example.CineHive.domain.media.repository;

import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.media.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * TMDB ID와 미디어 타입을 사용하여 미디어를 조회합니다.
     * "Find or Create" 패턴의 핵심 조회 메서드입니다.
     */
    Optional<Media> findByTmdbIdAndMediaType(Integer tmdbId, MediaType mediaType);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Media m SET m.likeCount = m.likeCount + 1 WHERE m.id = :mediaId")
    int increaseLikeCount(@Param("mediaId") Long mediaId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Media m SET m.likeCount = m.likeCount - 1 WHERE m.id = :mediaId AND m.likeCount > 0")
    int decreaseLikeCount(@Param("mediaId") Long mediaId);
}