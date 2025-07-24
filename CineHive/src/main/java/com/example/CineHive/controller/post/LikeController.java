package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.post.LikeCountResponse;
import com.example.CineHive.service.post.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 '좋아요' 관련 API 컨트롤러입니다.
 */
@Tag(name = "Like Controller", description = "게시글 '좋아요' 관련 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "게시글 '좋아요' 추가",
            description = """
               특정 게시글에 '좋아요'를 누릅니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - **Side Effect**: 이미 '싫어요'를 누른 상태였다면, '싫어요'는 자동으로 취소됩니다.
               """)
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.addLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("게시글에 '좋아요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '좋아요' 취소",
            description = """
               특정 게시글에 눌렀던 '좋아요'를 취소합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               """)
    @DeleteMapping
    public ResponseEntity<ApiResponse<MessageResponse>> removeLike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        likeService.removeLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("'좋아요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 '좋아요' 개수 조회",
            description = "특정 게시글의 '좋아요' 개수를 조회합니다. 인증이 필요 없습니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<LikeCountResponse>> getLikeCount(@PathVariable Long postId) {
        int count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.ok(new LikeCountResponse(count)));
    }
}