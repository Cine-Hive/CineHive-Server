package com.example.CineHive.repository.member;

import com.example.CineHive.entity.member.LoginHistory;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 특정 회원의 로그인 기록을 조회합니다.
     * 한 회원은 하나의 로그인 기록만 가지므로 Optional로 반환합니다.
     * @param member 조회할 회원 엔티티
     * @return 해당 회원의 LoginHistory (존재하지 않을 수 있음)
     */
    Optional<LoginHistory> findByMember(Member member);
}