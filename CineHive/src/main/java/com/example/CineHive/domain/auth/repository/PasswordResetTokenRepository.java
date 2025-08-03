package com.example.CineHive.domain.auth.controller.entity;

import com.example.CineHive.domain.user.controller.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findBySelector(String selector);

    Optional<PasswordResetToken> findByUser(User user);

    /**
     * 특정 시간 이전에 만료된 모든 토큰을 삭제합니다.
     * @param expiryDate 기준이 되는 만료 시간
     */
    void deleteAllByExpiryDateBefore(Instant expiryDate);
}