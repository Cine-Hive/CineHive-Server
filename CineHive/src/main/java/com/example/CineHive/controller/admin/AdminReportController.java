package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.global.ApiResponse;
import com.example.CineHive.dto.global.MessageResponse;
import com.example.CineHive.dto.report.ReportResponse;
import com.example.CineHive.entity.post.ReportStatus;
import com.example.CineHive.service.admin.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Report Controller", description = "신고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 내역 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @Parameter(description = "필터링할 신고 상태 (생략 시 전체 조회)")
            @RequestParam(required = false) ReportStatus status) {

        List<ReportResponse> reports = adminReportService.getReports(status);
        return ResponseEntity.ok(ApiResponse.ok(reports));
    }

    @Operation(summary = "신고 승인")
    @PatchMapping("/{reportId}/accept")
    public ResponseEntity<ApiResponse<MessageResponse>> acceptReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.acceptReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("신고를 성공적으로 승인 처리했습니다.")));
    }

    @Operation(summary = "신고 기각")
    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<MessageResponse>> rejectReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.rejectReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("신고를 성공적으로 기각 처리했습니다.")));
    }
}