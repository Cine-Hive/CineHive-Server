package com.example.CineHive.controller.replyController;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.service.reply.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
@Tag(name = "Reply Controller", description = "영화에 대한 감상평 작성 관련 기능을 제공하는 API")
public class ReplyController {

    private final ReplyService replyService;

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "해당 영화에 등록된 감상평 조회 ", description = "reply 테이블에 있는 감상평 중 해당 movieId에 해당하는 모든 감상평을 조회")
    public ResponseEntity<List<Reply>> getReplysByMovie(@PathVariable Long movieId) {
        return ResponseEntity.ok(replyService.getReplysByMovieId(movieId));
    }

    @GetMapping("/user/{email}")
    @Operation(summary = "특정 사용자의 모든 감상평 조회", description = "reply 테이블에 등록된 감상평 중 해당 email에 해당하는 모든 감상평을 조회")
    public ResponseEntity<List<Reply>> getReplysByMemEmail(@PathVariable String email) {
        return ResponseEntity.ok(replyService.getReplysByMemEmail(email));
    }

    @PostMapping
    @Operation(summary = "감상평 등록", description = "reply 테이블에 새로운 감상평을 등록하고, memnickname, mememail, movieId, content를 파라미터로 받아 저장")
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
    @Operation(summary = "감상평 삭제", description = "특정 영화에 등록된 특정 감상평을 삭제")
    public ResponseEntity<Void> deleteReply(@PathVariable Long movieId, @PathVariable Long replyId) {
        replyService.deleteReply(movieId, replyId);
        return ResponseEntity.noContent().build();
    }
}
