package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Network;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkRepository extends JpaRepository<Network, Long> {
}