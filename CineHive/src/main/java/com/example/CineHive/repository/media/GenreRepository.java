package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    /**
     * @deprecated 모든 장르가 COMMON으로 통합되었으므로 findById()를 사용하세요.
     */
    @Deprecated
    Optional<Genre> findByIdAndMediaType(Integer id, Genre.MediaType mediaType);
    
    // 필요한 경우 아래 메서드로 대체
    @Override
    Optional<Genre> findById(Integer id);
} 