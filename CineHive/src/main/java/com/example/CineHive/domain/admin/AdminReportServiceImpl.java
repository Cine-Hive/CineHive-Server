package com.example.CineHive.domain.admin;

import com.example.CineHive.domain.report.dto.ReportResponse;
import com.example.CineHive.domain.report.Report;
import com.example.CineHive.domain.report.ReportStatus;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.report.ReportRepository;
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
                .map(ReportResponse::from)
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
        log.info("관리자에 의해 신고(ID: {})가 승인 처리되었습니다.", reportId);
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = findReportById(reportId);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }

        report.reject();
        log.info("관리자에 의해 신고(ID: {})가 기각 처리되었습니다.", reportId);
    }

    private Report findReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }
}