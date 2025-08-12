package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvGenre;
import com.example.CineHive.datasync.domain.entity.TvGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TvGenreRepository extends JpaRepository<TvGenre, TvGenreId> {
    @Modifying
    @Query("DELETE FROM SyncTvGenre tg WHERE tg.tvId = :tvId")
    void deleteAllByTvId(@Param("tvId") Long tvId);
}