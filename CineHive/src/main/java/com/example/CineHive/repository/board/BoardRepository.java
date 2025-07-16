package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글(Board) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 * 기본적인 CRUD 외에 복잡한 쿼리가 필요한 경우 JPQL을 사용하여 메서드를 정의합니다.
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 키워드를 사용하여 게시글을 검색합니다.
     * 검색 범위는 게시글의 제목, 내용, 그리고 연관된 회원의 닉네임입니다.
     * 모든 비교는 대소문자를 구분하지 않습니다.
     *
     * @param keyword 검색할 키워드 문자열
     * @return 검색 조건에 맞는 게시글 엔티티의 리스트
     */
    @Query("SELECT b FROM Board b JOIN b.member m WHERE " +
            "LOWER(b.brdTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR b.brdContent LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(m.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Board> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 특정 회원이 작성한 모든 게시글을 데이터베이스에서 삭제합니다.
     * 이 메서드는 주로 회원 탈퇴와 같이 연관된 모든 데이터를 한 번에 정리해야 할 때 사용됩니다.
     * @Modifying 어노테이션은 이 쿼리가 데이터베이스 상태를 변경함을 나타냅니다.
     *
     * @param email 삭제할 게시글들의 작성자 이메일
     */
    @Modifying
    @Query("DELETE FROM Board b WHERE b.member.email = :email")
    void deleteByMember_Email(@Param("email") String email);
}
