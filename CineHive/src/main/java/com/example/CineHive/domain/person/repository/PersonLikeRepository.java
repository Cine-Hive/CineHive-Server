package com.example.CineHive.domain.person.repository;

import com.example.CineHive.domain.person.entity.Person;
import com.example.CineHive.domain.person.entity.PersonLike;
import com.example.CineHive.domain.user.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PersonLike 엔티티에 대한 데이터 접근을 처리하는 Repository입니다.
 */
public interface PersonLikeRepository extends JpaRepository<PersonLike, Long> {

    /**
     * 특정 사용자가 특정 인물을 이미 '좋아요' 했는지 확인합니다.
     *
     * @param user 사용자 엔티티
     * @param person 인물 엔티티
     * @return '좋아요' 존재 여부 (true/false)
     */
    boolean existsByUserAndPerson(User user, Person person);

    /**
     * 특정 인물에 대한 총 '좋아요' 개수를 집계합니다.
     *
     * @param person 집계할 인물 엔티티
     * @return 총 '좋아요' 수
     */
    long countByPerson(Person person);

    /**
     * 특정 사용자가 '좋아요'한 모든 인물-좋아요 관계 목록을 조회합니다.
     *
     * @param user 조회할 사용자 엔티티
     * @param sort 정렬 조건
     * @return List<PersonLike> '좋아요' 관계 목록
     */
    List<PersonLike> findAllByUser(User user, Sort sort);

    /**
     * 특정 사용자가 특정 인물에 대해 누른 '좋아요'를 삭제합니다.
     * 트랜잭션은 이 메서드를 호출하는 서비스 계층에서 관리합니다.
     *
     * @param user 사용자 엔티티
     * @param person 인물 엔티티
     */
    void deleteByUserAndPerson(User user, Person person);
}