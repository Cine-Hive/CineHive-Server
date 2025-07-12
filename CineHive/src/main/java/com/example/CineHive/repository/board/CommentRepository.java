package com.example.CineHive.repository.board;

import com.example.CineHive.entity.board.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [추가된 부분] Board의 ID를 기준으로 모든 Comment를 찾는 쿼리 메서드
    List<Comment> findByBoard_Id(Long boardId);

    // 회원 탈퇴를 위한 메서드 (AccountService에서 사용)
    void deleteByMember_Email(String email);
}