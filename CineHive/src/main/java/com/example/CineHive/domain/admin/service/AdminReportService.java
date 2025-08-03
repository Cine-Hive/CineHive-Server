package com.example.CineHive.domain.admin.service;

import com.example.CineHive.domain.report.dto.ReportResponse;
import com.example.CineHive.domain.report.entity.ReportStatus;

import java.util.List;

/**
 * 관리자의 신고 처리 관련 비즈니스 로직을 담당하는 서비스 인터페이스입니다.
 */
public interface AdminReportService {

    /**
     * 모든 신고 내역을 조회합니다. 상태(status)별로 필터링할 수 있습니다.
     *
     * @param status 필터링할 신고 상태 (PENDING, ACCEPTED, REJECTED). null일 경우 모든 상태를 조회합니다.
     * @return 신고 내역 DTO 리스트
     */
    List<ReportResponse> getReports(ReportStatus status);

    /**
     * 특정 신고를 '승인(ACCEPTED)' 상태로 변경합니다.
     * 신고 처리 후 관련 조치(예: 게시글 삭제)는 이 메서드 내에서 이루어질 수 있습니다.
     *
     * @param reportId 처리할 신고의 고유 ID
     */
    void acceptReport(Long reportId);

    /**
     * 특정 신고를 '기각(REJECTED)' 상태로 변경합니다.
     *
     * @param reportId 처리할 신고의 고유 ID
     */
    void rejectReport(Long reportId);
}