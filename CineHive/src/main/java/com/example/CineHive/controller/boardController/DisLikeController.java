package com.example.CineHive.controller.boardController;

import com.example.CineHive.service.board.DisLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DisLike Controller", description = "게시글의 싫어요를 등록, 취소 및 싫어요 수 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/dislike")
public class DisLikeController {

    @Autowired
    private DisLikeService disLikeService;

    // 싫어요 등록
    @Operation(summary = "싫어요 등록", description = "특정 게시글에 대한 싫어요를 등록")
    @PostMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> addDisLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isDisLiked = disLikeService.addDisLike(memEmail, boardId);
        return ResponseEntity.ok(isDisLiked ? "DisLiked" : "Already DisLiked");
    }

    // 싫어요 삭제
    @Operation(summary = "싫어요 취소", description = "특정 게시글에 대해 등록한 싫어요를 취소")
    @DeleteMapping("/{boardId}/users/{memEmail}")
    public ResponseEntity<String> removeDisLike(@PathVariable Long boardId, @PathVariable String memEmail) {
        boolean isRemoved = disLikeService.removeDisLike(memEmail, boardId);
        return isRemoved ? ResponseEntity.ok("Removed DisLike") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("DisLike Not Found");
    }

    // 특정 게시글의 싫어요 갯수 조회
    @Operation(summary = "싫어요 갯수 조회", description = "특정 게시글에 대해 싫어요의 총 갯수를 조회")
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getDisLikeCount(@PathVariable Long boardId) {
        int disLikeCount = disLikeService.getDisLikeCount(boardId);
        return ResponseEntity.ok(disLikeCount);
    }
}
