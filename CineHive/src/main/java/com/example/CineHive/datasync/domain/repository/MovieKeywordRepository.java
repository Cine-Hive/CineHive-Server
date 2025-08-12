package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.MovieKeyword;
import com.example.CineHive.datasync.domain.entity.MovieKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieKeywordRepository extends JpaRepository<MovieKeyword, MovieKeywordId> {
    @Modifying
    @Query("DELETE FROM SyncMovieKeyword mk WHERE mk.movieId = :movieId")
    void deleteAllByMovieId(@Param("movieId") Long movieId);
}