package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TmdbConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbConfigurationRepository extends JpaRepository<TmdbConfiguration, Short> {}