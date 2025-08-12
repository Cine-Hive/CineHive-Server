package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvCastRepository extends JpaRepository<TvCast, String> {
    @Modifying
    @Query("DELETE FROM SyncTvCast tc WHERE tc.tvId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}