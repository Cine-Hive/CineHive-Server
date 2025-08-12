package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.MovieCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieCrewRepository extends JpaRepository<MovieCrew, String> {
    @Modifying
    @Query("DELETE FROM SyncMovieCrew mc WHERE mc.movieId = :movieId")
    void deleteAllByMovieId(@Param("movieId") Long movieId);
}