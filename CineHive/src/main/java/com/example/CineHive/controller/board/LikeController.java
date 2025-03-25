package com.example.CineHive.controller.board;

import com.example.CineHive.service.board.LikeService;
import com.example.CineHive.util.JwtTokenUtil;  // JwtTokenUtil로 변경
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like Controller", description = "게시글의 좋아요를 등록, 취소 및 좋아요 수 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // 좋아요 추가
    @Operation(summary = "좋아요 등록", description = "특정 게시글에 대해 좋아요를 등록")
    @PostMapping("/{boardId}")
    public ResponseEntity<String> addLike(@PathVariable Long boardId, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            boolean isLiked = likeService.addLike(memEmail, boardId);
            return ResponseEntity.ok(isLiked ? "Liked" : "Already Liked");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    // 좋아요 삭제
    @Operation(summary = "좋아요 취소", description = "특정 게시글에 대해 등록한 좋아요를 취소")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> removeLike(@PathVariable Long boardId, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            boolean isRemoved = likeService.removeLike(memEmail, boardId);
            return isRemoved ? ResponseEntity.ok("Unliked") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Like Not Found");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    // 특정 게시글의 좋아요 갯수 조회
    @Operation(summary = "좋아요 갯수 조회", description = "특정 게시글에 대해 좋아요의 총 갯수를 조회")
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long boardId) {
        int likeCount = likeService.getLikeCount(boardId);
        return ResponseEntity.ok(likeCount);
    }
}
