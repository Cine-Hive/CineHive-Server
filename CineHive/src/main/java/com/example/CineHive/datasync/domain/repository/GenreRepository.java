package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
}