package com.example.CineHive.domain.collection.repository;

import com.example.CineHive.domain.collection.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
}