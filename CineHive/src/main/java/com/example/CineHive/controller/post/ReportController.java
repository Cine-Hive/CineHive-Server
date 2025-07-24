package com.example.CineHive.controller.report;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.report.ReportRequest;
import com.example.CineHive.service.report.ReportService;
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
 * 콘텐츠(게시글, 댓글) 신고 API 컨트롤러입니다.
 */
@Tag(name = "Report Controller", description = "콘텐츠(게시글, 댓글) 신고 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글 신고",
            description = """
               특정 게시글을 신고합니다.
               - **인증 필요**: `USER` 역할 이상의 권한이 필요합니다.
               - **규칙**: 자신의 게시글은 신고할 수 없으며, 동일한 게시글을 중복 신고할 수 없습니다.
               - 성공 시, `201 CREATED` 상태 코드가 반환됩니다.
               """)
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResponse<MessageResponse>> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        reportService.reportPost(postId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new MessageResponse("게시글 신고가 정상적으로 접수되었습니다.")));
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