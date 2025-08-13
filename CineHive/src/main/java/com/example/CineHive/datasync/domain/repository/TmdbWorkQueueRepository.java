package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TmdbWorkQueueRepository extends JpaRepository<TmdbWorkQueue, Long> {

    @Query("""
        SELECT w FROM TmdbWorkQueue w 
        WHERE w.processed = false 
        AND w.entityType = :entityType
        ORDER BY w.priority DESC, w.tmdbId ASC
        """)
    List<TmdbWorkQueue> findUnprocessedByEntityType(
        @Param("entityType") TmdbWorkQueue.EntityType entityType,
        Pageable pageable
    );

    @Query("""
        SELECT w FROM TmdbWorkQueue w 
        WHERE w.processed = false
        ORDER BY w.priority DESC, w.tmdbId ASC
        """)
    List<TmdbWorkQueue> findAllUnprocessed(Pageable pageable);

    @Query("""
        SELECT COUNT(w) FROM TmdbWorkQueue w 
        WHERE w.processed = false 
        AND w.entityType = :entityType
        """)
    long countUnprocessedByEntityType(@Param("entityType") TmdbWorkQueue.EntityType entityType);

    @Query("""
        SELECT COUNT(w) FROM TmdbWorkQueue w 
        WHERE w.processed = true 
        AND w.entityType = :entityType
        """)
    long countProcessedByEntityType(@Param("entityType") TmdbWorkQueue.EntityType entityType);

    boolean existsByEntityTypeAndTmdbId(
        TmdbWorkQueue.EntityType entityType,
        Long tmdbId
    );

    @Query("""
        SELECT w FROM TmdbWorkQueue w 
        WHERE w.entityType = :entityType 
        AND w.tmdbId = :tmdbId
        """)
    TmdbWorkQueue findByEntityTypeAndTmdbId(
        @Param("entityType") TmdbWorkQueue.EntityType entityType,
        @Param("tmdbId") Long tmdbId
    );

    int deleteByAttemptsGreaterThanEqual(int attempts);
    
    @Modifying
    @Transactional
    @Query("UPDATE TmdbWorkQueue w SET w.status = :status, w.processed = true, w.processedAt = CURRENT_TIMESTAMP WHERE w.tmdbId = :tmdbId AND w.entityType = :entityType")
    void updateStatus(@Param("tmdbId") Long tmdbId, @Param("entityType") String entityType, @Param("status") String status);
    
    @Modifying
    @Transactional
    @Query("UPDATE TmdbWorkQueue w SET w.status = :status, w.lastError = :error, w.attempts = w.attempts + 1 WHERE w.tmdbId = :tmdbId AND w.entityType = :entityType")
    void updateStatusWithError(@Param("tmdbId") Long tmdbId, @Param("entityType") String entityType, @Param("status") String status, @Param("error") String error);
}