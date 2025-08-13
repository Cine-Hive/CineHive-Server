package com.example.CineHive.datasync.domain.repository;

import com.example.CineHive.datasync.domain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("syncPersonRepository")
public interface PersonRepository extends JpaRepository<Person, Long> {
}