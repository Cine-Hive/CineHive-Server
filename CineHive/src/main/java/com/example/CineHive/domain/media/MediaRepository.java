<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/media/repository/MediaRepository.java
package com.example.CineHive.domain.media.repository;

import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.media.entity.MediaType;
=======
package com.example.CineHive.domain.media;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/media/MediaRepository.java

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