package com.example.CineHive.controller.board;

import com.example.CineHive.dto.comment.CommentDto;
import com.example.CineHive.dto.comment.CreateCommentRequest;
import com.example.CineHive.dto.comment.UpdateCommentRequest;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.service.board.CommentService;
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
import java.util.Map;

@Tag(name = "Comment Controller", description = "게시글 댓글 CRUD API")
@RestController
@RequestMapping("/api/v1/boards/{boardId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 게시글에 새로운 댓글을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CreateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        CommentDto createdComment = commentService.addComment(boardId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(createdComment));
    }

    @Operation(summary = "게시글의 모든 댓글 조회", description = "특정 게시글에 달린 모든 댓글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByBoard(@PathVariable Long boardId) {
        List<CommentDto> comments = commentService.getCommentsByBoard(boardId);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글의 내용을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long boardId, // boardId는 경로에 있지만, 권한 검증 등에 사용될 수 있으므로 유지
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        CommentDto updatedComment = commentService.updateComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedComment));
    }

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteComment(
            @PathVariable Long boardId, // boardId는 경로에 있지만, 권한 검증 등에 사용될 수 있으므로 유지
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "댓글이 성공적으로 삭제되었습니다.")));
    }
}