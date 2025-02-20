package com.example.CineHive.controller.boardController;

import com.example.CineHive.service.board.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like Controller", description = "게시글의 좋아요를 등록, 취소 및 좋아요 수 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // 좋아요 추가
    @Operation(summary = "좋아요 등록", description = "특정 게시글에 대해 좋아요를 등록")
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isLiked = likeService.addLike(memEmail, boardId);
        return ResponseEntity.ok(isLiked ? "Liked" : "Already Liked");
    }

    // 좋아요 삭제
    @Operation(summary = "좋아요 취소", description = "특정 게시글에 대해 등록한 좋아요를 취소")
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = likeService.removeLike(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Unliked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Like Not Found");
    }

    // 특정 게시글의 좋아요 개수 조회
    @Operation(summary = "좋아요 갯수 조회", description = "특정 게시글에 대해 좋아요의 전체 갯수를 조회")
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long boardId) {
        int likeCount = likeService.getLikeCount(boardId);
        return ResponseEntity.ok(likeCount);
    }
}