package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.MediaGenre;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaGenreRepository extends JpaRepository<MediaGenre, Long> {
    
    // 특정 장르에 속하는 미디어 ID 목록 조회
    List<MediaGenre> findByGenreIdAndMediaType(Integer genreId, Media.MediaType mediaType);
    
    // 특정 장르에 속하는 미디어 ID 목록 페이징 조회
    Page<MediaGenre> findByGenreIdAndMediaType(Integer genreId, Media.MediaType mediaType, Pageable pageable);
} 