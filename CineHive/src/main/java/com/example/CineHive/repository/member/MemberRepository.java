package com.example.CineHive.repository.member;

import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 이메일로 회원을 조회합니다.
     * @param email 조회할 이메일
     * @return Optional<Member> 객체 (null 대신 Optional을 사용하여 안전하게 처리)
     */
    Optional<Member> findByEmail(String email);

    /**
     * 닉네임으로 회원을 조회합니다.
     * @param nickname 조회할 닉네임
     * @return Optional<Member> 객체
     */
    Optional<Member> findByNickname(String nickname);

    /**
     * 해당 이메일이 DB에 존재하는지 확인합니다.
     * findByEmail().isPresent() 보다 성능상 이점이 있습니다.
     * @param email 확인할 이메일
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * 해당 닉네임이 DB에 존재하는지 확인합니다.
     * @param nickname 확인할 닉네임
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByNickname(String nickname);
}