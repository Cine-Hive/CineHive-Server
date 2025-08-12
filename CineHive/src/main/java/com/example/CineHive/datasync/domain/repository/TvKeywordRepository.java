package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvKeyword;
import com.example.CineHive.datasync.domain.entity.TvKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvKeywordRepository extends JpaRepository<TvKeyword, TvKeywordId> {
    @Modifying
    @Query("DELETE FROM SyncTvKeyword tk WHERE tk.tvId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}