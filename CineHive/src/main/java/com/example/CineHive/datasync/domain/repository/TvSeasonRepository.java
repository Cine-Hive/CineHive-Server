package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvSeasonRepository extends JpaRepository<TvSeason, Long> {
    @Modifying
    @Query("DELETE FROM SyncTvSeason ts WHERE ts.tvTmdbId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}