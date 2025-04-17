package com.example.CineHive.repository.media;

import com.example.CineHive.entity.credit.Crew;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {
    List<Crew> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
    
    // 특정 직무별 검색 (ex: 감독만 검색)
    List<Crew> findByMediaIdAndMediaTypeAndJob(Long mediaId, Media.MediaType mediaType, String job);
} 