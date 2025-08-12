package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.EpisodeCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeCastRepository extends JpaRepository<EpisodeCast, String> {
    @Modifying
    @Query("DELETE FROM SyncEpisodeCast ec WHERE ec.episodeId = :episodeId")
    void deleteAllByEpisodeId(@Param("episodeId") Long episodeId);
}