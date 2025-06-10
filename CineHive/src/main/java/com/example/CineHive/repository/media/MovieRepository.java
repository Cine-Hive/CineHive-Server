package com.example.CineHive.repository.media;

import com.example.CineHive.entity.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}