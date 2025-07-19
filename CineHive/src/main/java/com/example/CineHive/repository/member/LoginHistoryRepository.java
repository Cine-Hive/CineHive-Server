package com.example.CineHive.repository.member;

import com.example.CineHive.entity.user.LoginHistory;
import com.example.CineHive.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 로그인 기록(LoginHistory) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 특정 회원의 로그인 기록을 조회합니다.
     * 한 회원은 하나의 로그인 기록만 가지는 것을 전제로 하므로 Optional로 반환합니다.
     *
     * @param user 조회할 회원 엔티티
     * @return 존재할 경우 해당 회원의 LoginHistory를 담은 Optional, 존재하지 않을 경우 빈 Optional
     */
    Optional<LoginHistory> findByMember(User user);
}
