package com.example.CineHive.service.reply;

import com.example.CineHive.entity.reply.ReplyDisLike;
import com.example.CineHive.entity.reply.ReplyLike;
import com.example.CineHive.repository.reply.ReplyDisLikeRepository;
import com.example.CineHive.repository.reply.ReplyLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyJudgeService {

    @Autowired
    private ReplyLikeRepository replyLikeRepository;
    @Autowired
    private ReplyDisLikeRepository replyDisLikeRepository;

    @Transactional
    public boolean toggleLike(String memEmail, Long movieId, Long replyId) {
        if (replyLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)) {
            replyLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
            return false;
        }

        // 싫어요 누른 경우 해제 (좋아요와 싫어요는 동시 불가능)
        if(replyDisLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)){
            replyDisLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
        }

        ReplyLike newLike = new ReplyLike(null, memEmail, movieId, replyId);
        replyLikeRepository.save(newLike);
        return true;
    }

    @Transactional
    public boolean toggleDislike(String memEmail, Long movieId, Long replyId) {
        if (replyDisLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)) {
            replyDisLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
            return false;
        }

        // 좋아요 누른 경우 해제 (좋아요와 싫어요는 동시 불가능)
        if(replyLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)){
            replyLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
        }

        ReplyDisLike newDislike = new ReplyDisLike(null, memEmail, movieId, replyId);
        replyDisLikeRepository.save(newDislike);
        return true;
    }

    // 각 댓글의 좋아요 수 조회
    public long getLikeCount(Long replyId) {
        return replyLikeRepository.countByReplyId(replyId);
    }

    // 각 댓글의 싫어요 수 조회
    public long getDisLikeCount(Long replyId) {
        return replyDisLikeRepository.countByReplyId(replyId);
    }

}
