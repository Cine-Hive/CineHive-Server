package com.example.CineHive.domain.user.controller.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자(User) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일을 사용하여 사용자를 조회합니다.
     * @param email 조회할 사용자의 이메일
     * @return 사용자 정보를 담은 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임을 사용하여 사용자를 조회합니다.
     * @param nickname 조회할 사용자의 닉네임
     * @return 사용자 정보를 담은 Optional 객체
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 해당 이메일이 데이터베이스에 존재하는지 확인합니다.
     * @param email 확인할 이메일
     * @return 이메일이 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 해당 닉네임이 데이터베이스에 존재하는지 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 닉네임이 존재하면 true
     */
    boolean existsByNickname(String nickname);

    /**
     * 특정 이메일을 가진 사용자를 삭제합니다.
     * @param email 삭제할 사용자의 이메일
     */
    void deleteByEmail(String email);
}