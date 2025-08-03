package com.example.CineHive.domain.report.entity;

/**
 * 신고 처리 상태를 나타내는 Enum 클래스입니다.
 */
public enum ReportStatus {
    PENDING, // 신고가 접수되어 처리를 기다리는 상태입니다.

    ACCEPTED, // 관리자가 신고를 검토하고 승인한 상태입니다.

    REJECTED // 관리자가 신고를 검토하고 기각(거절)한 상태입니다.
}