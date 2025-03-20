package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyJudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/judge")
@RequiredArgsConstructor
public class ReplyJudgeController {

    @Autowired
    private ReplyJudgeService replyJudgeService;

    @PostMapping("/like")
    public ResponseEntity<String> toggleLike(@RequestParam String memEmail,@RequestParam Long movieId, @RequestParam Long replyId) {
        boolean isLiked = replyJudgeService.toggleLike(memEmail, movieId, replyId);
        return ResponseEntity.ok(isLiked ? "좋아요 완료" : "좋아요 취소");
    }


    @PostMapping("/dislike")
    public ResponseEntity<String> toggleDislike(@RequestParam String memEmail,@RequestParam Long movieId,@RequestParam Long replyId) {
        boolean isDisliked = replyJudgeService.toggleDislike(memEmail, movieId, replyId);
        return ResponseEntity.ok(isDisliked ? "싫어요 완료" : "싫어요 취소");
    }

    @GetMapping("/count/like")
    public ResponseEntity<Long> getLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getLikeCount(replyId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/dislike")
    public ResponseEntity<Long> getDisLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getDisLikeCount(replyId);
        return ResponseEntity.ok(count);
    }
}
