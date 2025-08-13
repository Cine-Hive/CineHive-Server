package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvSeriesNetwork;
import com.example.CineHive.datasync.domain.entity.TvSeriesNetworkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvSeriesNetworkRepository extends JpaRepository<TvSeriesNetwork, TvSeriesNetworkId> {
    @Modifying
    @Query("DELETE FROM SyncTvSeriesNetwork tsn WHERE tsn.tvId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}