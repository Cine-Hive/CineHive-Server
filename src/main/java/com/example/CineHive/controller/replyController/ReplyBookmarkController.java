package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyBookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/bookmark")
public class ReplyBookmarkController {

    @Autowired
    private ReplyBookmarkService replyBookmarkService;

    // 즐겨찾기 추가 요청
    @PostMapping("/toggle")
    public ResponseEntity<String> toggleBookmark(@RequestParam String memEmail, @RequestParam Long movieId) {
        boolean isBookmarked = replyBookmarkService.toggleBookmark(memEmail, movieId);
        return ResponseEntity.ok(isBookmarked ? "즐겨찾기 완료" : "즐겨찾기 취소");
    }

    @GetMapping("/count") // GET 요청으로 설정
    public ResponseEntity<Long> getBookmarkCount(@RequestParam Long movieId) {
        long count = replyBookmarkService.getBookmarkCount(movieId);
        return ResponseEntity.ok(count);
    }
}
