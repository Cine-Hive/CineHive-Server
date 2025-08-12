package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.EpisodeVideo;
import com.example.CineHive.datasync.domain.entity.EpisodeVideoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeVideoRepository extends JpaRepository<EpisodeVideo, EpisodeVideoId> {
    @Modifying
    @Query("DELETE FROM SyncEpisodeVideo ev WHERE ev.episodeTmdbId = :episodeId")
    void deleteAllByEpisodeId(@Param("episodeId") Long episodeId);
}