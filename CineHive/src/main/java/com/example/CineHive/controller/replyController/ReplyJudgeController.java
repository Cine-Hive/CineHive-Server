package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyJudgeService;
import com.example.CineHive.util.JwtTokenUtil; // 변경된 JwtTokenUtil 적용
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/reply/judge")
@RequiredArgsConstructor
public class ReplyJudgeController {

    @Autowired
    private ReplyJudgeService replyJudgeService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/like")
    public ResponseEntity<String> toggleLike(
            @RequestParam Long movieId,
            @RequestParam Long replyId,
            HttpServletRequest request) {

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isLiked = replyJudgeService.toggleLike(memEmail, movieId, replyId);
            return ResponseEntity.ok(isLiked ? "좋아요 완료" : "좋아요 취소");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/dislike")
    public ResponseEntity<String> toggleDislike(
            @RequestParam Long movieId,
            @RequestParam Long replyId,
            HttpServletRequest request) {

        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isDisliked = replyJudgeService.toggleDislike(memEmail, movieId, replyId);
            return ResponseEntity.ok(isDisliked ? "싫어요 완료" : "싫어요 취소");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
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
