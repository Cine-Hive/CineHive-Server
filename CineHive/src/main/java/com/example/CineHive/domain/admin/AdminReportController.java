package com.example.CineHive.domain.admin;

import com.example.CineHive.domain.common.dto.ApiResponse;
import com.example.CineHive.domain.common.dto.MessageResponse;
import com.example.CineHive.domain.report.dto.ReportResponse;
import com.example.CineHive.domain.report.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자의 신고 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin Report Controller", description = "신고 관리 API (관리자 전용)")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 내역 조회",
            description = "모든 신고 내역을 조회합니다. `status` 쿼리 파라미터를 사용하여 특정 상태(PENDING, ACCEPTED, REJECTED)의 신고만 필터링할 수 있습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @Parameter(description = "필터링할 신고 상태 (생략 시 전체 조회)")
            @RequestParam(required = false) ReportStatus status) {

        List<ReportResponse> reports = adminReportService.getReports(status);
        return ResponseEntity.ok(ApiResponse.ok(reports));
    }

    @Operation(summary = "신고 승인",
            description = "특정 신고를 '승인(ACCEPTED)' 처리합니다. 아직 처리되지 않은(PENDING) 신고에 대해서만 유효합니다.")
    @PatchMapping("/{reportId}/accept")
    public ResponseEntity<ApiResponse<MessageResponse>> acceptReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.acceptReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("신고를 성공적으로 승인 처리했습니다.")));
    }

    @Operation(summary = "신고 기각",
            description = "특정 신고를 '기각(REJECTED)' 처리합니다. 아직 처리되지 않은(PENDING) 신고에 대해서만 유효합니다.")
    @PatchMapping("/{reportId}/reject")
    public ResponseEntity<ApiResponse<MessageResponse>> rejectReport(
            @Parameter(description = "처리할 신고의 ID") @PathVariable Long reportId) {

        adminReportService.rejectReport(reportId);
        return ResponseEntity.ok(ApiResponse.ok(new MessageResponse("신고를 성공적으로 기각 처리했습니다.")));
    }

    @Operation(summary = "신고 처리", description = "신고를 승인 또는 기각 처리하고, 필요 시 후속 조치를 연동합니다.")
    @PatchMapping("/{reportId}/process")
    public void processReport(@PathVariable Long reportId) {
        // TODO: 1. ProcessReportRequest DTO (action: ACCEPTED/REJECTED, details: String)를 @RequestBody로 받음
        // TODO: 2. AdminReportService.processReport(reportId, request) 호출
        // TODO: 3. acceptReport, rejectReport를 대체하거나 내부적으로 호출하도록 구현
    }
}