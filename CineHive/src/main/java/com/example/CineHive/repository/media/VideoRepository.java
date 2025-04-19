package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Video;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    List<Video> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
    
    // 특정 타입의 비디오만 조회 (ex: 타입이 "Trailer"인 경우만)
    List<Video> findByMediaIdAndMediaTypeAndType(Long mediaId, Media.MediaType mediaType, String type);
} 