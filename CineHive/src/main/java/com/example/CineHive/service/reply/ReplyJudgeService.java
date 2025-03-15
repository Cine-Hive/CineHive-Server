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
        // 🔍 중복 좋아요 검사 (`existsByMemEmailAndReplyId()` 사용)
        if (replyLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)) {
            replyLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
            return false;
        }

        // 존재하지 않으면 추가
        ReplyLike newLike = new ReplyLike(null, memEmail, movieId, replyId);
        replyLikeRepository.save(newLike);
        return true;
    }

    @Transactional
    public boolean toggleDislike(String memEmail, Long movieId, Long replyId) {
        // 🔍 중복 싫어요 검사 (`existsByMemEmailAndReplyId()` 사용)
        if (replyDisLikeRepository.existsByMemEmailAndReplyId(memEmail, replyId)) {
            replyDisLikeRepository.deleteByMemEmailAndReplyId(memEmail, replyId);
            return false;
        }

        // 존재하지 않으면 추가
        ReplyDisLike newDislike = new ReplyDisLike(null, memEmail, movieId, replyId);
        replyDisLikeRepository.save(newDislike);
        return true;
    }


    public long getLikeCount(Long replyId) {  // ✅ replyId 기준으로 변경
        return replyLikeRepository.countByReplyId(replyId);
    }

    public long getDisLikeCount(Long movieId) {  // ✅ replyId 기준으로 변경
        return replyDisLikeRepository.countByReplyId(movieId);
    }

}
