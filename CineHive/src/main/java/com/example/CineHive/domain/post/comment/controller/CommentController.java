package com.example.CineHive.domain.post.controller.comment;

import com.example.CineHive.domain.post.comment.dto.CommentResponse;
import com.example.CineHive.domain.post.comment.dto.CreateCommentRequest;
import com.example.CineHive.domain.post.comment.dto.UpdateCommentRequest;
import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.MessageResponse;
import com.example.CineHive.domain.common.dto.PageResponse;
import com.example.CineHive.domain.report.controller.ReportService;
import com.example.CineHive.domain.report.dto.ReportRequest;
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
 * 게시글 댓글과 관련된 모든 CRUD 및 신고 API를 담당하는 통합 컨트롤러입니다.
 */
@Tag(name = "Comment Controller", description = "게시글 댓글 CRUD API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ReportService reportService;

    @Operation(summary = "특정 게시글에 댓글 작성",
            description = """
            ### **특정 게시글에 새로운 댓글을 작성합니다.**
            
            **[인증]**
            - 이 API를 호출하려면 `Authorization` 헤더에 유효한 **Access Token**이 반드시 필요합니다. (`USER` 역할 이상)
            
            **[요청]**
            - `POST` 메서드로, Request Body에 댓글 내용을 담은 JSON을 전송해야 합니다.
              ```json
              {
                "content": "이 영화 정말 재밌네요!"
              }
              ```
            
            **[응답]**
            - 성공 시 `201 CREATED` 상태 코드와 함께, 방금 생성된 댓글의 전체 정보(`CommentResponse`)를 반환합니다.
            
            **[클라이언트 처리]**
            - 응답으로 받은 댓글 정보를 사용하여 UI의 댓글 목록 최상단에 새로운 댓글을 즉시 추가할 수 있습니다.
            """)
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse commentResponse = commentService.addComment(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(commentResponse));
    }

    @Operation(summary = "특정 게시글의 댓글 목록 페이징 조회",
            description = """
            ### **특정 게시글에 달린 댓글 목록을 페이징하여 조회합니다.**
            
            **[인증]**
            - 이 API는 인증이 필요 없는 공개 API입니다.
            
            **[요청]**
            - `GET` 메서드로, `page`(페이지 번호)와 `size`(페이지 당 항목 수)를 Query Parameter로 전달할 수 있습니다.
            - 값을 보내지 않으면 기본값(`page=1`, `size=10`)이 적용됩니다.
            - 예시: `/api/v1/posts/123/comments?page=2&size=20`
            
            **[응답]**
            - 페이징 정보(`page`, `totalPages` 등)와 함께 댓글 목록(`content`)이 포함된 `PagedResponse` 객체를 반환합니다.
            
            **[클라이언트 처리]**
            - 응답의 페이징 정보를 바탕으로 '더 보기' 버튼이나 페이지네이션 UI를 구현할 수 있습니다.
            """)
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getCommentsByPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CommentResponse> comments = commentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(ApiResponse.ok(comments));
    }

    @Operation(summary = "댓글 수정",
            description = """
            ### **자신이 작성한 댓글의 내용을 수정합니다.**
            
            **[인증 및 권한]**
            - `Authorization` 헤더에 유효한 **Access Token**이 필요합니다.
            - **오직 댓글을 작성한 본인만** 이 API를 호출할 수 있습니다. 다른 사용자의 댓글 수정을 시도하면 `403 Forbidden` 에러가 발생합니다.
            
            **[요청]**
            - `PUT` 메서드로, Request Body에 수정할 내용을 담은 JSON을 전송합니다.
              ```json
              {
                "content": "수정된 댓글 내용입니다."
              }
              ```
            
            **[응답]**
            - 성공 시 `200 OK` 상태 코드와 함께, 수정이 완료된 댓글의 전체 정보(`CommentResponse`)를 반환합니다.
            """)
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        CommentResponse updatedComment = commentService.updateComment(commentId, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(updatedComment));
    }

    @Operation(summary = "댓글 삭제",
            description = """
            ### **자신이 작성한 댓글을 삭제합니다.**
            
            **[인증 및 권한]**
            - `Authorization` 헤더에 유효한 **Access Token**이 필요합니다.
            - **오직 댓글을 작성한 본인 또는 관리자만** 이 API를 호출할 수 있습니다.
            
            **[응답]**
            - 성공 시 `200 OK` 상태 코드와 함께 성공 메시지를 반환합니다.
            
            **[클라이언트 처리]**
            - 이 API 호출 성공 시, UI의 댓글 목록에서 해당 댓글을 제거해야 합니다.
            """)
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("댓글이 성공적으로 삭제되었습니다.")));
    }

    @Operation(summary = "댓글 신고",
            description = """
            ### **부적절한 내용의 댓글을 신고합니다.**
            
            **[인증 및 규칙]**
            - `Authorization` 헤더에 유효한 **Access Token**이 필요합니다.
            - **자신이 작성한 댓글은 신고할 수 없습니다.** (호출 시 `400 Bad Request` 에러 발생)
            - **동일한 댓글을 중복해서 신고할 수 없습니다.** (호출 시 `409 Conflict` 에러 발생)
            
            **[요청]**
            - `POST` 메서드로, Request Body에 신고 사유를 담은 JSON을 전송합니다.
              ```json
              {
                "reason": "광고성 댓글입니다."
              }
              ```
            
            **[응답]**
            - 성공 시 `201 CREATED` 상태 코드와 함께 신고 접수 완료 메시지를 반환합니다.
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
