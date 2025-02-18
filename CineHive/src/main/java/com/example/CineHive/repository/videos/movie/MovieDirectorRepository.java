package com.example.CineHive.repository.videos.movie;

import com.example.CineHive.entity.credit.movie.Director;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieDirectorRepository extends JpaRepository<Director, Long> {
    Optional<Director> findByName(String name);
}