package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardDislike;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 게시글 '싫어요'(BoardDislike) 엔티티에 대한 데이터 접근을 처리하는 JpaRepository입니다.
 */
@Repository
public interface DislikeRepository extends JpaRepository<BoardDislike, Long> {

    /**
     * 특정 회원과 게시글에 해당하는 '싫어요' 정보가 존재하는지 확인합니다.
     *
     * @param member 확인할 회원 엔티티
     * @param board  확인할 게시글 엔티티
     * @return '싫어요'가 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByMemberAndBoard(Member member, Board board);

    /**
     * 특정 회원과 게시글로 '싫어요' 정보를 조회합니다.
     *
     * @param member 조회할 회원 엔티티
     * @param board  조회할 게시글 엔티티
     * @return 존재할 경우 '싫어요' 정보를 담은 Optional, 존재하지 않을 경우 빈 Optional
     */
    Optional<BoardDislike> findByMemberAndBoard(Member member, Board board);

    /**
     * 특정 게시글에 대한 모든 '싫어요'의 개수를 조회합니다.
     *
     * @param boardId 개수를 조회할 게시글의 ID
     * @return 해당 게시글의 '싫어요' 개수
     */
    int countByBoard_Id(Long boardId);

    /**
     * 특정 회원이 누른 모든 '싫어요'를 데이터베이스에서 삭제합니다.
     * 이 메서드는 주로 회원 탈퇴 시 연관된 데이터를 정리할 때 사용됩니다.
     *
     * @param email 삭제할 '싫어요'의 소유자 이메일
     */
    @Modifying
    @Query("DELETE FROM BoardDislike d WHERE d.member.email = :email")
    void deleteByMember_Email(@Param("email") String email);
}
