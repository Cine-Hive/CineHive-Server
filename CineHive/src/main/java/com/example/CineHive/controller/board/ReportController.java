package com.example.CineHive.controller.board;

import com.example.CineHive.dto.report.ReportRequest;
import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.service.board.ReportService;
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

import java.util.Map;

@Tag(name = "Report Controller", description = "콘텐츠(게시글, 댓글) 신고 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 특정 게시글을 신고합니다.
     * 한 사용자는 동일한 게시글을 한 번만 신고할 수 있습니다.
     *
     * @param boardId       신고할 게시글의 ID
     * @param request       신고 사유를 담은 DTO
     * @param userDetails   인증된 사용자 정보 (Spring Security가 자동으로 주입)
     * @return 성공 메시지를 담은 ApiResponse
     */
    @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다.")
    @PostMapping("/boards/{boardId}/reports")
    public ResponseEntity<ApiResponse<Map<String, String>>> reportBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody ReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        reportService.reportBoard(boardId, request.reason(), userDetails.getUsername());

        // 새로운 '신고' 리소스가 생성되었으므로 201 CREATED 상태 코드를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("message", "게시글 신고가 정상적으로 접수되었습니다.")));
    }

    /**
     * 특정 댓글을 신고합니다.
     * 한 사용자는 동일한 댓글을 한 번만 신고할 수 있습니다.
     *
     * @param commentId     신고할 댓글의 ID
     * @param request       신고 사유를 담은 DTO
     * @param userDetails   인증된 사용자 정보 (Spring Security가 자동으로 주입)
     * @return 성공 메시지를 담은 ApiResponse
     */
    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다.")
    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<ApiResponse<Map<String, String>>> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        reportService.reportComment(commentId, request.reason(), userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("message", "댓글 신고가 정상적으로 접수되었습니다.")));
    }
}