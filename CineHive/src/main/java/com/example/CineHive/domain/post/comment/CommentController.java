package com.example.CineHive.domain.post.comment;

import com.example.CineHive.global.common.dto.ApiResponse;
import com.example.CineHive.global.common.dto.MessageResponse;
import com.example.CineHive.domain.report.dto.ReportRequest;
import com.example.CineHive.domain.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final ReportService reportService;

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

    @Operation(summary = "댓글 신고",
            description = """
               특정 댓글을 신고합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - **규칙**: 자신의 댓글은 신고할 수 없으며, 동일한 댓글을 중복 신고할 수 없습니다.
               - 성공 시, `201 CREATED` 상태 코드가 반환됩니다.
               """)
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<MessageResponse>> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        reportService.reportComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new MessageResponse("댓글 신고가 정상적으로 접수되었습니다.")));
    }
}