package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.Bookmark;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 특정 회원과 게시글로 북마크 정보를 조회합니다.
     * @param member 조회할 회원 엔티티
     * @param board 조회할 게시글 엔티티
     * @return Optional<Bookmark>
     */
    Optional<Bookmark> findByMemberAndBoard(Member member, Board board);

    /**
     * 특정 회원과 게시글에 해당하는 북마크를 삭제합니다.
     * @param member 삭제할 북마크의 회원 엔티티
     * @param board 삭제할 북마크의 게시글 엔티티
     */
    void deleteByMemberAndBoard(Member member, Board board);

    /**
     * 특정 게시글의 북마크 개수를 카운트합니다.
     * @param boardId 카운트할 게시글의 ID
     * @return 북마크 개수
     */
    int countByBoard_Id(Long boardId);

    /**
     * 특정 회원의 모든 북마크를 삭제합니다. (회원 탈퇴 시 사용)
     * @param email 삭제할 회원의 이메일
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.member.email = :email")
    void deleteByMember_Email(@Param("email") String email);

    /**
     * 특정 회원과 게시글에 해당하는 북마크가 존재하는지 확인합니다.
     *
     * @param member 회원 엔티티
     * @param board  게시글 엔티티
     * @return 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByMemberAndBoard(Member member, Board board);
}