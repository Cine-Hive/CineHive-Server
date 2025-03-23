package com.example.CineHive.controller.replyController;

import com.example.CineHive.entity.reply.Reply;
import com.example.CineHive.service.reply.ReplyService;
import com.example.CineHive.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {

    @Autowired
    private  ReplyService replyService;
    @Autowired
    private  JwtTokenUtil jwtTokenUtil;

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

    @PostMapping
    public ResponseEntity<?> createReply(@RequestParam String memNickname,
                                         @RequestParam Long movieId,
                                         @RequestParam String content,
                                         HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            if (content.length() > 1000) {
                return ResponseEntity.badRequest().body("감상평은 1000자를 초과할 수 없습니다.");
            }
            Reply reply = new Reply(memNickname, memEmail, movieId, content);
            return ResponseEntity.ok(replyService.saveReply(reply));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

    @DeleteMapping("/{movieId}/{replyId}")
    public ResponseEntity<?> deleteReply(@PathVariable Long movieId,
                                         @PathVariable Long replyId,
                                         HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 필요합니다.");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            replyService.deleteReply(movieId, replyId, memEmail);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }
    }

}
