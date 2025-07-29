package com.example.CineHive.domain.auth;

import com.example.CineHive.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 로그인 기록(LoginHistory) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 특정 사용자의 로그인 기록을 조회합니다.
     * @param user 조회할 사용자 엔티티
     * @return 해당 사용자의 LoginHistory를 담은 Optional 객체
     */
    Optional<LoginHistory> findByUser(User user);
}