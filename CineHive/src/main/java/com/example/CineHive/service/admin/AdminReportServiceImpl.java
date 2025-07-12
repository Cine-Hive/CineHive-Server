package com.example.CineHive.service.admin;

import com.example.CineHive.dto.report.ReportResponse;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.exception.ReportNotFoundException;
import com.example.CineHive.repository.board.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportRepository reportRepository;

    @Override
    public List<ReportResponse> getReports(ReportStatus status) {
        List<Report> reports;
        if (status == null) {
            // 상태 필터가 없으면 모든 신고 내역 조회
            reports = reportRepository.findAll();
        } else {
            // 특정 상태의 신고 내역만 조회
            reports = reportRepository.findByStatus(status);
        }
        return reports.stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptReport(Long reportId) {
        Report report = findReportById(reportId);

        // 이미 처리된 신고는 상태를 변경하지 않음
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        report.accept(); // 엔티티의 상태를 ACCEPTED로 변경
        log.info("Report {} has been accepted by admin.", reportId);

        // TODO: 신고 승인 후속 조치 구현
        // 예: 신고된 게시글/댓글 비활성화, 작성자에게 경고 알림 등
        // if (report.getBoard() != null) { boardService.deactivateByAdmin(report.getBoard().getId()); }
        // if (report.getComment() != null) { commentService.deactivateByAdmin(report.getComment().getId()); }
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = findReportById(reportId);

        // 이미 처리된 신고는 변경하지 않음
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        report.reject(); // 엔티티의 상태를 REJECTED로 변경
        log.info("Report {} has been rejected by admin.", reportId);
    }

    //== 내부 헬퍼 메서드 ==//
    private Report findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고를 찾을 수 없습니다: " + reportId));
    }
}