package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvSeasonCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvSeasonCrewRepository extends JpaRepository<TvSeasonCrew, String> {
    @Modifying
    @Query("DELETE FROM SyncTvSeasonCrew tsc WHERE tsc.seasonId = :seasonId")
    void deleteAllBySeasonId(@Param("seasonId") Long seasonId);
}