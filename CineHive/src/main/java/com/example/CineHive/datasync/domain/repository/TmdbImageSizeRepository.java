package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.TmdbImageSize;
import com.example.CineHive.datasync.domain.entity.TmdbImageSizeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbImageSizeRepository extends JpaRepository<TmdbImageSize, TmdbImageSizeId> {}