package com.example.CineHive.controller.replyController;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.service.reply.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // 특정 영화의 모든 리뷰 조회
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<Reply>> getReplysByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(replyService.getReplysByMovieId(movieId));
    }

    // 특정 사용자의 모든 리뷰 조회
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Reply>> getReplysByMemEmail(@PathVariable String email) {
        return ResponseEntity.ok(replyService.getReplysByMemEmail(email));
    }

    // 리뷰 저장 (생성)
    @PostMapping
    public ResponseEntity<?> createReply(@RequestParam String memNickname,
                                         @RequestParam String memEmail,
                                         @RequestParam Long movieId,
                                         @RequestParam String content) {
        if (content.length() > 1000) {  // ✅ 글자 수 제한 (예: 1000자)
            return ResponseEntity.badRequest().body("감상평은 1000자를 초과할 수 없습니다.");
        }

        Reply reply = new Reply(memNickname, memEmail, movieId, content);
        return ResponseEntity.ok(replyService.saveReply(reply));
    }


    @DeleteMapping("/{movieId}/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long movieId, @PathVariable Long replyId) {
        replyService.deleteReply(movieId, replyId);
        return ResponseEntity.noContent().build();
    }
}
