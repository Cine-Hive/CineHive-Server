package com.example.CineHive.repository.media;

import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {
    /**
     * 미디어 ID와 타입으로 제작진 목록 조회
     */
    List<Crew> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
} 