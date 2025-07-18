package com.example.CineHive.entity.board;

public enum ReportStatus {
    PENDING,  // 처리 대기 중
    ACCEPTED, // 처리 완료 (신고 승인)
    REJECTED  // 기각 (신고 거절)
}