package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvCrewRepository extends JpaRepository<TvCrew, String> {
    @Modifying
    @Query("DELETE FROM SyncTvCrew tc WHERE tc.tvId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}