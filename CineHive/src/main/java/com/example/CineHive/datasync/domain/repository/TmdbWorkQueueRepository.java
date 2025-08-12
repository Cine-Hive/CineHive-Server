package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TmdbWorkQueue;
import com.example.CineHive.datasync.domain.entity.TmdbWorkQueueId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbWorkQueueRepository extends JpaRepository<TmdbWorkQueue, TmdbWorkQueueId> {
}