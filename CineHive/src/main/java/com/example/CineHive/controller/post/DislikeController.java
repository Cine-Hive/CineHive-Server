package com.example.CineHive.controller.post;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.post.DislikeCountResponse;
import com.example.CineHive.service.post.DislikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 '싫어요' 관련 API 컨트롤러입니다.
 */
@Tag(name = "Dislike Controller", description = "게시글 '싫어요' 관련 API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/dislikes")
@RequiredArgsConstructor
public class DislikeController {

    private final DislikeService dislikeService;

    @Operation(summary = "게시글 '싫어요' 추가",
            description = """
               특정 게시글에 '싫어요'를 누릅니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - **Side Effect**: 이미 '좋아요'를 누른 상태였다면, '좋아요'는 자동으로 취소됩니다.
               """)
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> addDislike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.addDislike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("게시글에 '싫어요'를 눌렀습니다.")));
    }

    @Operation(summary = "게시글 '싫어요' 취소",
            description = """
               특정 게시글에 눌렀던 '싫어요'를 취소합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               """)
    @DeleteMapping
    public ResponseEntity<ApiResponse<MessageResponse>> removeDislike(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        dislikeService.removeDislike(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("'싫어요'를 취소했습니다.")));
    }

    @Operation(summary = "게시글 '싫어요' 개수 조회",
            description = "특정 게시글의 '싫어요' 개수를 조회합니다. 인증이 필요 없습니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<DislikeCountResponse>> getDislikeCount(@PathVariable Long postId) {
        int count = dislikeService.getDislikeCount(postId);
        return ResponseEntity.ok(ApiResponse.ok(new DislikeCountResponse(count)));
    }
}