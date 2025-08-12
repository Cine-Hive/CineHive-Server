package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.EpisodeImage;
import com.example.CineHive.datasync.domain.entity.EpisodeImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImage, EpisodeImageId> {
    @Modifying
    @Query("DELETE FROM SyncEpisodeImage ei WHERE ei.episodeTmdbId = :episodeId")
    void deleteAllByEpisodeId(@Param("episodeId") Long episodeId);
}