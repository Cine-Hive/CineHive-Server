package com.example.CineHive.repository.media;

import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CastRepository extends JpaRepository<Cast, Long> {
    /**
     * 미디어 ID와 타입으로 출연진 목록 조회
     */
    List<Cast> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
} 