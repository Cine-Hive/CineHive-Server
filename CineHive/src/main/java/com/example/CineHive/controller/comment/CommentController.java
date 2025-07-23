package com.example.CineHive.controller.comment;

import com.example.CineHive.dto.comment.CommentResponse;
import com.example.CineHive.dto.comment.CreateCommentRequest;
import com.example.CineHive.dto.comment.UpdateCommentRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.service.post.CommentService;
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

import java.util.List;

/**
 * 게시글 댓글 CRUD API 컨트롤러입니다.
 */
@Tag(name = "Comment Controller", description = "게시글 댓글 CRUD API")
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성",
            description = """
               특정 게시글에 새로운 댓글을 등록합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - 성공 시, 생성된 댓글의 정보와 함께 `201 CREATED` 상태 코드가 반환됩니다.
               """)
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse createdComment = commentService.addComment(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdComment));
    }

    @Operation(summary = "게시글의 모든 댓글 조회",
            description = "특정 게시글에 달린 모든 댓글 목록을 조회합니다. 인증이 필요 없습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @Operation(summary = "댓글 수정",
            description = """
               자신이 작성한 댓글의 내용을 수정합니다.
               - **인증 및 권한 필요**: 댓글을 작성한 본인만 수정할 수 있습니다.
               - 타인의 댓글 수정 시도 시 `403 Forbidden` 에러가 발생합니다.
               """)
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        CommentResponse updatedComment = commentService.updateComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedComment));
    }

    @Operation(summary = "댓글 삭제",
            description = """
               자신이 작성한 댓글을 삭제합니다.
               - **인증 및 권한 필요**: 댓글을 작성한 본인 또는 `ADMIN` 역할만 삭제할 수 있습니다.
               - 타인의 댓글 삭제 시도 시 `403 Forbidden` 에러가 발생합니다.
               """)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("댓글이 성공적으로 삭제되었습니다.")));
    }
}