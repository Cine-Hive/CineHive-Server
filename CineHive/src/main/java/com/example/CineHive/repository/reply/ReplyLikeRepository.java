package com.example.CineHive.repository.reply;

import com.example.CineHive.entity.User;
import com.example.CineHive.entity.reply.ReplyLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long>{
    boolean existsByMemEmailAndReplyId(String memEmail, Long replyId);
    void deleteByMemEmailAndReplyId(String memEmail, Long replyId);
    long countByReplyId(Long replyId);

    List<ReplyLike> findByMemEmail(String memEmail);

}
