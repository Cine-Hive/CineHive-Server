package com.example.CineHive.repository.ott;

import com.example.CineHive.entity.ott.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {
}
