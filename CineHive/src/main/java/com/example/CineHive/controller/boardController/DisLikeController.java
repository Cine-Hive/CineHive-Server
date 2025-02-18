package com.example.CineHive.controller.boardController;

import com.example.CineHive.service.board.DisLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dislike")
public class DisLikeController {

    @Autowired
    private DisLikeService disLikeService;

    // 싫어요 추가
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addDisLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isDisLiked = disLikeService.addDisLike(memEmail, boardId);
        return ResponseEntity.ok(isDisLiked ? "DisLiked" : "Already DisLiked");
    }

    // 싫어요 삭제
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeDisLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = disLikeService.removeDisLike(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Removed DisLike") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("DisLike Not Found");
    }

    // 특정 게시글의 싫어요 개수 조회
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getDisLikeCount(@PathVariable Long boardId) {
        int disLikeCount = disLikeService.getDisLikeCount(boardId);
        return ResponseEntity.ok(disLikeCount);
    }
}
