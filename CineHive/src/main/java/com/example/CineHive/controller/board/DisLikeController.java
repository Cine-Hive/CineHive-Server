package com.example.CineHive.controller.board;

import com.example.CineHive.service.board.DisLikeService;
import com.example.CineHive.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DisLike Controller", description = "게시글의 싫어요를 등록, 취소 및 싫어요 수 조회 기능을 제공하는 API")
@RestController
@RequestMapping("/dislike")
public class DisLikeController {

    @Autowired
    private DisLikeService disLikeService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // 싫어요 등록
    @Operation(summary = "싫어요 등록", description = "특정 게시글에 대한 싫어요를 등록")
    @PostMapping("/{boardId}")
    public ResponseEntity<String> addDisLike(@PathVariable Long boardId, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            boolean isDisLiked = disLikeService.addDisLike(memEmail, boardId);
            return ResponseEntity.ok(isDisLiked ? "DisLiked" : "Already DisLiked");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    // 싫어요 삭제
    @Operation(summary = "싫어요 취소", description = "특정 게시글에 대해 등록한 싫어요를 취소")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> removeDisLike(@PathVariable Long boardId, HttpServletRequest request) {
        String token = jwtTokenUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        try {
            String memEmail = jwtTokenUtil.extractUsername(token);

            boolean isRemoved = disLikeService.removeDisLike(memEmail, boardId);
            return isRemoved ? ResponseEntity.ok("Removed DisLike") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("DisLike Not Found");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    // 특정 게시글의 싫어요 갯수 조회
    @Operation(summary = "싫어요 갯수 조회", description = "특정 게시글에 대해 싫어요의 총 갯수를 조회")
    @GetMapping("/{boardId}/count")
    public ResponseEntity<Integer> getDisLikeCount(@PathVariable Long boardId) {
        int disLikeCount = disLikeService.getDisLikeCount(boardId);
        return ResponseEntity.ok(disLikeCount);
    }
}
