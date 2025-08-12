package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.MovieProductionCompany;
import com.example.CineHive.datasync.domain.entity.MovieProductionCompanyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieProductionCompanyRepository extends JpaRepository<MovieProductionCompany, MovieProductionCompanyId> {
    @Modifying
    @Query("DELETE FROM SyncMovieProductionCompany mpc WHERE mpc.movieId = :movieId")
    void deleteAllByMovieId(@Param("movieId") Long movieId);
}