package com.example.CineHive.repository;

import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    Optional<LoginHistory> findByUser(User user);
}
