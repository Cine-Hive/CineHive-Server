package com.example.CineHive.domain.auth.controller.entity;

import com.example.CineHive.domain.user.controller.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    /**
     * 특정 사용자의 비밀번호 이력을 최신순으로 조회합니다.
     * PESSIMISTIC_WRITE 잠금을 사용하여 동시성 문제를 방지합니다.
     * @param user 사용자
     * @return 비밀번호 이력 목록
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 시간 이전에 생성된 모든 비밀번호 히스토리를 삭제합니다.
     * @param cutoff 기준이 되는 시간 (이 시간 이전의 모든 기록이 삭제됨)
     */
    void deleteAllByCreatedAtBefore(Instant cutoff);
}