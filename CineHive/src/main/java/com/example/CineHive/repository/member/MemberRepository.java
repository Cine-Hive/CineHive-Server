package com.example.CineHive.repository.member;

import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 회원(Member) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일을 사용하여 회원을 조회합니다. 이메일은 고유한 값으로 간주됩니다.
     *
     * @param email 조회할 회원의 이메일
     * @return 존재할 경우 회원 정보를 담은 Optional, 존재하지 않을 경우 빈 Optional
     */
    Optional<Member> findByEmail(String email);

    /**
     * 닉네임을 사용하여 회원을 조회합니다. 닉네임은 고유한 값으로 간주됩니다.
     *
     * @param nickname 조회할 회원의 닉네임
     * @return 존재할 경우 회원 정보를 담은 Optional, 존재하지 않을 경우 빈 Optional
     */
    Optional<Member> findByNickname(String nickname);

    /**
     * 해당 이메일이 데이터베이스에 존재하는지 확인합니다.
     * findByEmail().isPresent() 보다 성능상 이점이 있습니다.
     *
     * @param email 확인할 이메일
     * @return 이메일이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByEmail(String email);

    /**
     * 해당 닉네임이 데이터베이스에 존재하는지 확인합니다.
     *
     * @param nickname 확인할 닉네임
     * @return 닉네임이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByNickname(String nickname);

    /**
     * 특정 이메일을 가진 회원을 데이터베이스에서 삭제합니다.
     *
     * @param email 삭제할 회원의 이메일
     */
    void deleteByEmail(String email);
}
