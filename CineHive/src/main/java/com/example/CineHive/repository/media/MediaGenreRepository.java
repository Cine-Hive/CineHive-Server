package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.MediaGenre;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaGenreRepository extends JpaRepository<MediaGenre, Long> {
    
    // 특정 미디어의 장르 정보 조회
    List<MediaGenre> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
} 