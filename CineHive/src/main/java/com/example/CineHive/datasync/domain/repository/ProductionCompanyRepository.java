package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.ProductionCompany;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionCompanyRepository extends JpaRepository<ProductionCompany, Long> {
}