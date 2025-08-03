package com.example.CineHive.domain.admin.controller.entity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 대시보드 API 컨트롤러입니다.
 */
@Tag(name = "Admin Dashboard Controller", description = "관리자용 대시보드 API")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    @Operation(summary = "대시보드 통계 조회")
    @GetMapping("/stats")
    public void getDashboardStats() {
        // TODO: 1. 신규 가입자, 게시글/댓글 수, DAU 등 핵심 지표 조회 서비스 호출
        // TODO: 2. DashboardStatsResponse DTO로 변환하여 반환
    }

    @Operation(summary = "미처리 신고 현황 요약 조회")
    @GetMapping("/reports-summary")
    public void getPendingReportsSummary() {
        // TODO: 1. ReportService에서 PENDING 상태인 신고 건수 조회
        // TODO: 2. ReportSummaryResponse DTO로 변환하여 반환
    }
}