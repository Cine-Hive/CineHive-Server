package com.example.CineHive.controller.admin;

import com.example.CineHive.dto.report.ReportResponse;
import com.example.CineHive.dto.response.ApiResponse;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.service.admin.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin Report Controller", description = "신고 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 내역 조회", description = "모든 신고 내역을 조회합니다. 상태(PENDING, ACCEPTED, REJECTED)별 필터링이 가능합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @Parameter(description = "필터링할 신고 상태 (생략 시 전체 조회)")
            @RequestParam(required = false) ReportStatus status) {

        List<ReportResponse> reports = adminReportService.getReports(status);
        return ResponseEntity.ok(ApiResponse.ok(reports));
    }

    @Operation(summary = "신고 승인", description = "특정 신고를 '승인(ACCEPTED)' 처리합니다. 신고된 콘텐츠에 대한 후속 조치가 트리거될 수 있습니다.")
    @PatchMapping("/{reportId}/accept")
    public ResponseEntity<ApiResponse<Map<String, String>>> acceptReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.acceptReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "신고를 성공적으로 승인 처리했습니다.")));
    }

    @Operation(summary = "신고 기각", description = "특정 신고를 '기각(REJECTED)' 처리합니다.")
    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<Map<String, String>>> rejectReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.rejectReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("message", "신고를 성공적으로 기각 처리했습니다.")));
    }
}