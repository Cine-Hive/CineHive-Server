package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    @Modifying
    @Query("DELETE FROM SyncEpisode e WHERE e.seasonTmdbId = :seasonId")
    void deleteAllBySeasonId(@Param("seasonId") Long seasonId);
}