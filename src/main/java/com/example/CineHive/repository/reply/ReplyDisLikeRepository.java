package com.example.CineHive.repository.reply;

import com.example.CineHive.entity.reply.ReplyDisLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReplyDisLikeRepository extends JpaRepository<ReplyDisLike, Long> {
    boolean existsByMemEmailAndReplyId(String memEmail, Long replyId);
    void deleteByMemEmailAndReplyId(String memEmail, Long replyId);
    long countByReplyId(Long replyId);
}
