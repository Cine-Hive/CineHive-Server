package com.example.CineHive.controller.board;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.service.board.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Like Controller", description = "게시글 '좋아요' 관련 API")
@RestController
@RequestMapping("/api/v1/boards/{boardId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시글 '좋아요' 추가", description = "특정 게시글에 '좋아요'를 누릅니다. 이미 '싫어요'를 누른 경우, '싫어요'는 자동으로 취소됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> addLike(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.addLike(boardId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "게시글에 '좋아요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '좋아요' 취소", description = "특정 게시글에 눌렀던 '좋아요'를 취소합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> removeLike(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.removeLike(boardId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "'좋아요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 '좋아요' 개수 조회", description = "특정 게시글의 '좋아요' 개수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getLikeCount(@PathVariable Long boardId) {
        int count = likeService.getLikeCount(boardId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("likeCount", count)));
    }
}