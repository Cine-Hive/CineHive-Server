package com.example.CineHive.service.admin;

import com.example.CineHive.dto.report.ReportResponse;
import com.example.CineHive.entity.board.Report;
import com.example.CineHive.entity.board.ReportStatus;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
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
            reports = reportRepository.findAll();
        } else {
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

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        report.accept();
        log.info("Report {} has been accepted by admin.", reportId);
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = findReportById(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        report.reject();
        log.info("Report {} has been rejected by admin.", reportId);
    }

    private Report findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }
}