package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Optional<Genre> findByIdAndMediaType(Integer id, Genre.MediaType mediaType);
    List<Genre> findAllByMediaType(Genre.MediaType mediaType);
} 