package com.example.CineHive.repository.ott;

import com.example.CineHive.entity.ott.Ott;
import com.example.CineHive.entity.ott.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OttRepository extends JpaRepository<Ott, Long> {
    List<Ott> findByProvider(Provider provider);
}
