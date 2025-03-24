package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyJudgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/judge")
@RequiredArgsConstructor
@Tag(name = "ReplyJudge Controller", description = "좋아요/싫어요 관련 기능을 제공하는 API")
public class ReplyJudgeController {

    @Autowired
    private ReplyJudgeService replyJudgeService;

    @PostMapping("/like")
    @Operation(summary = "감상평에 좋아요 등록", description = "reply_likes 테이블에 해당 감상평 추가")
    public ResponseEntity<String> toggleLike(@RequestParam String memEmail,@RequestParam Long movieId, @RequestParam Long replyId) {
        boolean isLiked = replyJudgeService.toggleLike(memEmail, movieId, replyId);
        return ResponseEntity.ok(isLiked ? "좋아요 완료" : "좋아요 취소");
    }


    @PostMapping("/dislike")
    @Operation(summary = "감상평에 싫어요 등록", description = "reply_dislikes 테이블에 해당 감상평 추가")
    public ResponseEntity<String> toggleDislike(@RequestParam String memEmail,@RequestParam Long movieId,@RequestParam Long replyId) {
        boolean isDisliked = replyJudgeService.toggleDislike(memEmail, movieId, replyId);
        return ResponseEntity.ok(isDisliked ? "싫어요 완료" : "싫어요 취소");
    }

    @GetMapping("/count/like")
    @Operation(summary = "감상평에 등록된 좋아요 카운트", description = "reply_likes 테이블에 replyId로 조회하여 좋아요 수 반환")
    public ResponseEntity<Long> getLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getLikeCount(replyId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/dislike")
    @Operation(summary = "감상평에 등록된 싫어요 카운트", description = "reply_dislikes 테이블에 replyId로 조회하여 싫어요 수 반환")
    public ResponseEntity<Long> getDisLikeCount(@RequestParam Long replyId) {
        long count = replyJudgeService.getDisLikeCount(replyId);
        return ResponseEntity.ok(count);
    }
}
