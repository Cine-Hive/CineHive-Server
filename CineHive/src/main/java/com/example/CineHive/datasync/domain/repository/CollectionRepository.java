package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
}