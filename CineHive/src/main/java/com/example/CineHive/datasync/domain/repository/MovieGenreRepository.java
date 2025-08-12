package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.MovieGenre;
import com.example.CineHive.datasync.domain.entity.MovieGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, MovieGenreId> {
    @Modifying
    @Query("DELETE FROM MovieGenre mg WHERE mg.movieId = :movieId")
    void deleteAllByMovieId(@Param("movieId") Long movieId);
}