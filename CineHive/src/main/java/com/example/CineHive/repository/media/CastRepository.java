package com.example.CineHive.repository.media;

import com.example.CineHive.entity.credit.Cast;
import com.example.CineHive.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CastRepository extends JpaRepository<Cast, Long> {
    List<Cast> findByMediaIdAndMediaTypeOrderByOrder(Long mediaId, Media.MediaType mediaType);
} 