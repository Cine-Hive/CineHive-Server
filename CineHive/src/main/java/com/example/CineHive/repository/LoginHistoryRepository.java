package com.example.CineHive.repository;

import com.example.CineHive.entity.LoginHistory;
import com.example.CineHive.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    Optional<LoginHistory> findByUser(User user);
}
