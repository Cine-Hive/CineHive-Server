package com.example.CineHive.controller.board;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.service.post.DislikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Dislike Controller", description = "게시글 '싫어요' 관련 API")
@RestController
@RequestMapping("/api/v1/boards/{boardId}/dislikes")
@RequiredArgsConstructor
public class DislikeController {

    private final DislikeService dislikeService;

    @Operation(summary = "게시글 '싫어요' 추가", description = "특정 게시글에 '싫어요'를 누릅니다. 이미 '좋아요'를 누른 경우, '좋아요'는 자동으로 취소됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> addDislike(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.addDislike(boardId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "게시글에 '싫어요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '싫어요' 취소", description = "특정 게시글에 눌렀던 '싫어요'를 취소합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> removeDislike(
            @PathVariable Long boardId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.removeDislike(boardId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "'싫어요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 '싫어요' 개수 조회", description = "특정 게시글의 '싫어요' 개수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getDislikeCount(@PathVariable Long boardId) {
        int count = dislikeService.getDislikeCount(boardId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("dislikeCount", count)));
    }
}