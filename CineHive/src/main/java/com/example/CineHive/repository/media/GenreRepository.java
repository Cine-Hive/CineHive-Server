package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}