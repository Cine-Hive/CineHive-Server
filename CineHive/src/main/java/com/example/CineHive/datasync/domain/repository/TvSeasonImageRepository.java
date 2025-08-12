package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvSeasonImage;
import com.example.CineHive.datasync.domain.entity.TvSeasonImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvSeasonImageRepository extends JpaRepository<TvSeasonImage, TvSeasonImageId> {
    @Modifying
    @Query("DELETE FROM SyncTvSeasonImage tsi WHERE tsi.seasonTmdbId = :seasonId")
    void deleteAllBySeasonId(@Param("seasonId") Long seasonId);
}