package com.example.CineHive.service.reply;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.repository.reply.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    // 특정 영화의 리뷰 가져오기
    public List<Reply> getReplysByMovieId(Long movieId) {
        return replyRepository.findByMovieId(movieId);
    }

    // 특정 사용자의 리뷰 가져오기
    public List<Reply> getReplysByMemEmail(String email) {
        return replyRepository.findByMemEmail(email);
    }

    // 리뷰 저장
    @Transactional
    public Reply saveReply(Reply reply) {
        return replyRepository.save(reply);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReply(Long movieId, Long replyId, String memEmail) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        if (!reply.getMovieId().equals(movieId)) {
            throw new RuntimeException("해당 영화의 리뷰가 아닙니다.");
        }

        if (!reply.getMemEmail().equals(memEmail)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        replyRepository.delete(reply);
    }


}
