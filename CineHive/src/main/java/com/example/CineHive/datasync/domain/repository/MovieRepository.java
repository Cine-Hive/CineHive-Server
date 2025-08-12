package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
}