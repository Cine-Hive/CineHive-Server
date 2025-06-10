package com.example.CineHive.repository.credit;

import com.example.CineHive.entity.credit.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}