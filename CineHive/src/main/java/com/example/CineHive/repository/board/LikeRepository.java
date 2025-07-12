package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Board;
import com.example.CineHive.entity.board.BoardLike;
import com.example.CineHive.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<BoardLike, Long> {

    Optional<BoardLike> findByMemberAndBoard(Member member, Board board);

    int countByBoard_Id(Long boardId);

    void deleteByMember_Email(String email);
    boolean existsByMemberAndBoard(Member member, Board board);
}