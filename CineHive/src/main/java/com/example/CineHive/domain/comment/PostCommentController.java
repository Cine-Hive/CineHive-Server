package com.example.CineHive.domain.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 특정 게시글에 대한 댓글 목록 조회 및 생성을 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Comment Controller", description = "게시글 댓글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

    // TODO: private final CommentService commentService;

    @Operation(summary = "특정 게시글의 모든 댓글 조회")
    @GetMapping
    public void getCommentsByPost(@PathVariable Long postId) {
        // TODO: 1. CommentService.getCommentsByPost(postId) 호출 (페이징 고려)
        // TODO: 2. PagedResponse<CommentResponse> 또는 List<CommentResponse> 형태로 반환
    }

    @Operation(summary = "특정 게시글에 댓글 작성")
    @PostMapping
    public void addComment(
            @PathVariable Long postId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. CreateCommentRequest DTO를 @RequestBody로 받음
        // TODO: 2. CommentService.addComment(postId, request, userEmail) 호출
        // TODO: 3. 성공(201 CREATED) 시 생성된 CommentResponse 반환
    }
}