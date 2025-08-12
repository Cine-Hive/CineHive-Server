package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {
}