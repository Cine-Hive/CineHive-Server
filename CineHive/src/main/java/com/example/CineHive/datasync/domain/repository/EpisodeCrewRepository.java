package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.EpisodeCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeCrewRepository extends JpaRepository<EpisodeCrew, String> {
    @Modifying
    @Query("DELETE FROM SyncEpisodeCrew ec WHERE ec.episodeId = :episodeId")
    void deleteAllByEpisodeId(@Param("episodeId") Long episodeId);
}