package com.example.CineHive.controller.replyController;

import com.example.CineHive.service.reply.ReplyBookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply/bookmark")
@Tag(name = "ReplyBookMark Controller", description = "영화 즐겨찾기 관련 기능을 제공하는 API")
public class ReplyBookmarkController {

    @Autowired
    private ReplyBookmarkService replyBookmarkService;

    // 즐겨찾기 추가 요청
    @PostMapping("/toggle")
    @Operation(summary = "해당 영화 즐겨찾기 등록", description = "reply_bookmark 테이블에 해당 영화가 즐겨찾기가 있다면 즐겨찾기 취소, 없다면 즐겨찾기 등록")
    public ResponseEntity<String> toggleBookmark(@RequestParam String memEmail, @RequestParam Long movieId) {
        boolean isBookmarked = replyBookmarkService.toggleBookmark(memEmail, movieId);
        return ResponseEntity.ok(isBookmarked ? "즐겨찾기 완료" : "즐겨찾기 취소");
    }

    @GetMapping("/count") // GET 요청으로 설정
    @Operation(summary = "해당 영화의 즐겨찾기 수 조회", description = "reply_bookmark 테이블에 해당 영화의 즐겨찾기 수를 movie_id로 조회")
    public ResponseEntity<Long> getBookmarkCount(@RequestParam Long movieId) {
        long count = replyBookmarkService.getBookmarkCount(movieId);
        return ResponseEntity.ok(count);
    }
}
