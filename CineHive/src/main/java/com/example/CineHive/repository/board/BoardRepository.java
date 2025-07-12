package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 키워드를 사용하여 게시글의 제목, 내용, 작성자 닉네임에서 검색합니다.
     * @param keyword 검색할 키워드
     * @return 검색된 게시글 목록
     */
    @Query("SELECT b FROM Board b JOIN b.member m WHERE " +
            "LOWER(b.brdTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR b.brdContent LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(m.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Board> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 특정 회원이 작성한 모든 게시글을 삭제합니다. (회원 탈퇴 시 사용)
     * @param email 삭제할 회원의 이메일
     */
    @Modifying
    @Query("DELETE FROM Board b WHERE b.member.email = :email")
    void deleteByMember_Email(@Param("email") String email);
}