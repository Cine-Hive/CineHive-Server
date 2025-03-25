package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyBookmarkService;
import com.example.CineHive.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/bookmark")
public class ReplyBookmarkController {

    @Autowired
    private ReplyBookmarkService replyBookmarkService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/toggle")
    public ResponseEntity<String> toggleBookmark(@RequestParam Long movieId, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(401).body("토큰이 필요합니다.");
        }
        try {
            String memEmail = jwtTokenUtil.extractUsername(token);
            boolean isBookmarked = replyBookmarkService.toggleBookmark(memEmail, movieId);
            return ResponseEntity.ok(isBookmarked ? "즐겨찾기 완료" : "즐겨찾기 취소");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getBookmarkCount(@RequestParam Long movieId) {
        long count = replyBookmarkService.getBookmarkCount(movieId);
        return ResponseEntity.ok(count);
    }
}
