package com.example.CineHive.controller.boardController;

import com.example.CineHive.service.board.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // 좋아요 추가
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isLiked = likeService.addLike(memEmail, boardId);
        return ResponseEntity.ok(isLiked ? "Liked" : "Already Liked");
    }

    // 좋아요 삭제
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = likeService.removeLike(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Unliked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Like Not Found");
    }

    // 특정 게시글의 좋아요 개수 조회
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long boardId) {
        int likeCount = likeService.getLikeCount(boardId);
        return ResponseEntity.ok(likeCount);
    }
}