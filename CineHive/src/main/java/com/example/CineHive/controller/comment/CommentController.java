package com.example.CineHive.controller.comment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 개별 댓글 수정, 삭제, 신고를 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Comment Controller", description = "게시글 댓글 CRUD API")
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    // TODO: private final CommentService commentService;
    // TODO: private final ReportService reportService;

    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    public void updateComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. UpdateCommentRequest DTO를 @RequestBody로 받음
        // TODO: 2. CommentService.updateComment(commentId, request, userEmail) 호출
        // TODO: 3. 성공 시 수정된 CommentResponse 반환
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. CommentService.deleteComment(commentId, userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "댓글 신고")
    @PostMapping("/{commentId}/reports")
    public void reportComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. ReportRequest DTO를 @RequestBody로 받음
        // TODO: 2. ReportService.reportComment(commentId, request, userEmail) 호출
        // TODO: 3. 성공(201 CREATED) 시 MessageResponse 반환
    }
}