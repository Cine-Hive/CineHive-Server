package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Media;
import com.example.CineHive.entity.media.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
    /**
     * 미디어 ID와 타입으로 비디오 목록 조회
     */
    List<Video> findByMediaIdAndMediaType(Long mediaId, Media.MediaType mediaType);
} 